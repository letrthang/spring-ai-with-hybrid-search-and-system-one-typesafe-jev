package com.example.ragdemo.ui;

import com.example.ragdemo.chat.ChatHistoryService;
import com.example.ragdemo.chat.ChatMessage;
import com.example.ragdemo.chat.ChatSession;
import com.example.ragdemo.rag.RagAnswer;
import com.example.ragdemo.rag.RagService;
import com.example.ragdemo.rag.ServiceStatus;
import com.example.ragdemo.rag.ServiceStatus.Service;
import com.example.ragdemo.system1.SystemOneHealth;
import com.example.ragdemo.system2.SystemTwoHealth;
import com.example.ragdemo.weaviate.WeaviateHealth;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 💬 Chat — the main demo screen.
 *
 * <ul>
 *   <li><b>Status:</b> a pill for each required service (🗄️ Weaviate, ⚡ System 1, 🧠 System 2), checked when the page
 *       opens and every {@value #RECHECK_SECONDS} s. If any is not 🟢, a ⚠️ banner says which and why, and the
 *       question box and Ask are <b>disabled</b> — no message can be sent (owner ruling 2026-10-16).</li>
 *   <li><b>Ask:</b> runs {@link RagService} on a virtual thread; the answer bubble shows 📚 Sources, 🔬 Trace and
 *       🔀 Data flow.</li>
 *   <li><b>📂 History:</b> every conversation, saved in the local DB. Click a row for its card, click again to close,
 *       double-click to read it in a dialog.</li>
 * </ul>
 */
@Route("")
@PageTitle("Chat · RAG demo")
public class ChatView extends Div {

    static final int RECHECK_SECONDS = 30;
    static final int MAX_QUESTION = 2000;
    static final List<String> EXAMPLES = List.of(
            "What does the NGG PAM sequence do for Cas9?",
            "Why does warm water hold less oxygen than cold water?",
            "What does Newton's second law say?");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final RagService rag;
    private final ChatHistoryService history;
    private final Map<Service, Supplier<ServiceStatus>> checks = new EnumMap<>(Service.class);
    private final Map<Service, StatusPill> pills = new EnumMap<>(Service.class);
    private final Map<Service, ServiceStatus> statuses = new EnumMap<>(Service.class);

    private final Div banner = new Div();
    private final Div messages = new Div();
    private final TextArea question = new TextArea();
    private final Span counter = new Span();
    private final Button ask = new Button("🔎 Ask");
    private final ProgressBar progress = new ProgressBar();
    private final Map<RagService.Stage, Span> steps = new EnumMap<>(RagService.Stage.class);
    private final Div stepRow = new Div();
    private final Div examples = new Div();
    private final Grid<ChatSession> sessions = new Grid<>();
    private final MasterDetailLayout historyLayout = new MasterDetailLayout();
    private final Div historyCard = new Div();
    private final Span sessionCount = new Span();

    private String sessionId;   // the conversation the next question belongs to (null = a new one)
    private boolean busy;
    private Registration pollRegistration;

    public ChatView(RagService rag, ChatHistoryService history, WeaviateHealth weaviate, SystemOneHealth systemOne,
                    SystemTwoHealth systemTwo) {
        this.rag = rag;
        this.history = history;
        checks.put(Service.WEAVIATE, weaviate::check);
        checks.put(Service.SYSTEM1, systemOne::check);
        checks.put(Service.SYSTEM2, systemTwo::check);
        addClassNames("p-4", "flex", "flex-col", "gap-4", "max-w-screen-xl", "box-border");

        Paragraph intro = new Paragraph("Ask a question. Weaviate finds passages (BM25 + vector), ⚡ System 1 keeps "
                + "only the useful ones, 🧠 System 2 writes the answer. Open 🔀 Data flow under an answer to see "
                + "every step.");
        intro.addClassNames("rd-muted", "m-0");
        add(new H2("💬 Chat"), intro, statusRow(), banner, conversationCard(), questionCard(), historyCard());
        banner.addClassNames("rd-warning", "p-3", "flex", "flex-wrap", "gap-2", "items-center");
        banner.setVisible(false);
        applyEnabled();
    }

    // ================================================================== layout

    private Div statusRow() {
        Div row = new Div();
        row.addClassNames("flex", "flex-wrap", "gap-2", "items-center");
        for (Service s : Service.values()) {
            StatusPill pill = new StatusPill(s);
            pills.put(s, pill);
            row.add(pill);
        }
        Span every = new Span("re-checked every " + RECHECK_SECONDS + " s");
        every.addClassNames("rd-muted", "text-xs");
        row.add(every);
        return row;
    }

    private Div conversationCard() {
        H3 title = new H3("🗨️ Conversation");
        title.addClassNames("m-0", "text-base");
        Button newChat = new Button("＋ New chat", e -> startNewChat());
        newChat.addThemeVariants(ButtonVariant.TERTIARY);
        Div head = new Div(title, newChat);
        head.addClassNames("flex", "justify-between", "items-center");
        messages.addClassNames("rd-messages", "flex", "flex-col", "gap-3");
        showEmptyConversation();
        Div card = new Div(head, messages);
        card.addClassNames("rd-card", "p-4", "flex", "flex-col", "gap-3");
        return card;
    }

    private Div questionCard() {
        H3 title = new H3("💬 Your question");
        title.addClassNames("m-0", "text-base");
        question.setPlaceholder("Ask about biotech or physics, e.g. \"" + EXAMPLES.getFirst() + "\"");
        question.setMaxLength(MAX_QUESTION);
        question.setValueChangeMode(ValueChangeMode.EAGER);
        question.addValueChangeListener(e -> {
            updateCounter();
            applyEnabled();
        });
        question.setWidthFull();
        question.setMinHeight("5em");
        Shortcuts.addShortcutListener(question, this::submit, Key.ENTER, KeyModifier.CONTROL)
                .listenOn(question);
        updateCounter();
        counter.addClassNames("rd-muted", "text-sm");

        ask.addThemeVariants(ButtonVariant.PRIMARY);
        ask.addClickListener(e -> submit());
        Span tip = new Span("Ctrl + Enter to send");
        tip.addClassNames("rd-muted", "text-xs");
        Div actions = new Div(ask, counter, tip);
        actions.addClassNames("flex", "flex-wrap", "gap-3", "items-center");

        for (String example : EXAMPLES) {
            Button b = new Button(example, e -> question.setValue(example));
            b.addThemeVariants(ButtonVariant.TERTIARY);
            b.addClassNames("text-sm", "rd-example");
            examples.add(b);
        }
        Span try_ = new Span("Try:");
        try_.addClassNames("rd-muted", "text-sm");
        examples.addComponentAsFirst(try_);
        examples.addClassNames("flex", "flex-wrap", "gap-1", "items-center");

        progress.setIndeterminate(true);
        progress.setVisible(false);   // shown by applyEnabled() while a question runs
        for (RagService.Stage stage : RagService.Stage.values()) {
            Span chip = new Span();
            chip.addClassNames("rd-status", "rd-step");
            steps.put(stage, chip);
            stepRow.add(chip);
        }
        stepRow.addClassNames("flex", "flex-wrap", "gap-2", "items-center");
        stepRow.setVisible(false);    // appears with the first question, then keeps the last outcome
        resetSteps();

        Div card = new Div(title, question, actions, examples, progress, stepRow);
        card.addClassNames("rd-card", "p-4", "flex", "flex-col", "gap-3");
        return card;
    }

    private Div historyCard() {
        sessions.addColumn(s -> TIME.format(s.getStartedAt())).setHeader("Started").setWidth("11em").setFlexGrow(0);
        sessions.addColumn(ChatSession::getTitle).setHeader("Title").setFlexGrow(3);
        sessions.addColumn(ChatSession::getMessageCount).setHeader("Messages").setWidth("7em").setFlexGrow(0);
        sessions.addColumn(s -> TIME.format(s.getLastMessageAt())).setHeader("Last message").setWidth("11em")
                .setFlexGrow(0);
        sessions.getColumns().forEach(c -> c.setResizable(true));
        sessions.addThemeVariants(GridVariant.ROW_STRIPES);
        sessions.setEmptyStateText("No conversations yet.");
        // lazy: the grid asks for one page at a time (local DB, fast)
        sessions.setItems(query -> history.sessions(query.getOffset(), query.getLimit()).stream(),
                query -> (int) history.countSessions());
        sessions.asSingleSelect().addValueChangeListener(e -> historyLayout.setDetail(
                e.getValue() == null ? null : sessionCard(e.getValue())));
        sessions.addItemDoubleClickListener(e -> {
            sessions.select(e.getItem());
            openConversationDialog(e.getItem());
        });
        sessions.setSizeFull();

        historyLayout.setMaster(sessions);
        historyLayout.setMasterSize("min(560px, 100%)");
        historyLayout.setDetailSize("360px");
        historyLayout.addBackdropClickListener(e -> sessions.deselectAll());
        historyLayout.addDetailEscapePressListener(e -> sessions.deselectAll());
        historyLayout.setSizeFull();

        Span title = new Span("📂 History");
        title.addClassNames("font-semibold");
        sessionCount.addClassNames("rd-status", "rd-status-UNKNOWN", "text-xs");
        Span hint = new Span("Click selects · click again to close · double-click a row for 🔍 Details…");
        hint.addClassNames("rd-muted", "text-xs", "hidden", "md:inline");
        Button fullScreen = new Button("⛶", e -> toggleFullScreen());
        fullScreen.addThemeVariants(ButtonVariant.TERTIARY);
        fullScreen.getElement().setAttribute("title", "Full screen (Esc to exit)");
        fullScreen.getElement().setAttribute("aria-label", "Full screen");
        Div left = new Div(title, sessionCount);
        left.addClassNames("flex", "gap-2", "items-center");
        Div right = new Div(hint, fullScreen);
        right.addClassNames("flex", "gap-2", "items-center");
        Div head = new Div(left, right);
        head.addClassNames("flex", "justify-between", "items-center", "gap-2");

        historyCard.add(head, historyLayout);
        historyCard.addClassNames("rd-card", "p-3", "flex", "flex-col", "gap-2", "h-[420px]");
        Shortcuts.addShortcutListener(historyCard, () -> {
            if (historyCard.hasClassName("rd-fullscreen")) toggleFullScreen();
        }, Key.ESCAPE).listenOn(historyCard);
        refreshHistory();
        return historyCard;
    }

    private DetailCard sessionCard(ChatSession s) {
        Span when = new Span("🕒 " + TIME.format(s.getStartedAt()) + " · " + s.getMessageCount() + " messages");
        when.addClassNames("rd-muted", "text-sm");
        Div questions = new Div();
        questions.addClassNames("flex", "flex-col", "gap-1", "text-sm");
        for (ChatMessage m : history.messages(s.getId())) {
            if (m.getSender() == ChatMessage.Sender.USER) questions.add(new Span("💬 " + m.getText()));
        }
        Button open = new Button("🔍 Details…", e -> openConversationDialog(s));
        Button continue_ = new Button("▶ Continue here", e -> loadConversation(s));
        continue_.addThemeVariants(ButtonVariant.PRIMARY);
        Div buttons = new Div(continue_, open);
        buttons.addClassNames("flex", "flex-wrap", "gap-2");
        return new DetailCard("🗨️ " + s.getTitle(), sessions::deselectAll, when, questions, buttons);
    }

    // ================================================================== service status (fail closed)

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        UI ui = event.getUI();
        ui.setPollInterval(RECHECK_SECONDS * 1000);
        pollRegistration = ui.addPollListener(e -> checkAll());
        checkAll();
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (pollRegistration != null) pollRegistration.remove();   // a closed page never keeps polling
        event.getUI().setPollInterval(-1);
        super.onDetach(event);
    }

    private void checkAll() {
        for (Service s : Service.values()) {
            Background.run(this, checks.get(s), this::show);   // the three checks run at the same time
        }
    }

    private void show(ServiceStatus status) {
        statuses.put(status.service(), status);
        pills.get(status.service()).show(status);
        applyEnabled();
    }

    private boolean allReady() {
        return statuses.size() == Service.values().length && statuses.values().stream().allMatch(ServiceStatus::isReady);
    }

    /** The question box and Ask are usable only when all three services are 🟢 and no question is running. */
    private void applyEnabled() {
        boolean ready = allReady();
        question.setEnabled(ready && !busy);
        ask.setEnabled(ready && !busy && !question.getValue().isBlank());
        examples.setEnabled(ready && !busy);
        progress.setVisible(busy);

        List<ServiceStatus> notReady = statuses.values().stream().filter(s -> !s.isReady()).toList();
        banner.removeAll();
        banner.setVisible(!notReady.isEmpty());
        if (notReady.isEmpty()) return;
        Span lead = new Span("⚠️ Chat is disabled until every service is 🟢:");
        lead.addClassName("font-semibold");
        banner.add(lead);
        for (ServiceStatus s : notReady) banner.add(new Span(s.service().label + " — " + s.reason() + "."));
        if (notReady.stream().anyMatch(s -> s.reason().contains("Key Management"))) {
            banner.add(new RouterLink("🔑 Open Key Management", KeyManagementView.class));
        }
        if (notReady.stream().anyMatch(s -> s.reason().contains("Search Data"))) {
            banner.add(new RouterLink("🔎 Open Search Data", SearchDataView.class));
        }
    }

    // ================================================================== asking

    private void submit() {
        String q = question.getValue().strip();
        if (q.isEmpty() || busy || !allReady()) return;
        busy = true;
        applyEnabled();
        if (sessionId == null) messages.removeAll();
        messages.add(AnswerBubble.question(q));
        scrollToEnd();
        resetSteps();
        stepRow.setVisible(true);
        UI ui = UI.getCurrent();
        Background.run(this, () -> {
            // each stage reports itself as it starts; paint it at once (we are on the worker thread here)
            RagAnswer answer = rag.ask(q, progress -> ui.access(() -> showStep(progress)));
            String id = history.saveTurn(sessionId, answer);
            return new Turn(answer, id);
        }, turn -> {
            busy = false;
            sessionId = turn.sessionId();
            messages.add(AnswerBubble.answer(turn.answer()));
            finishSteps(turn.answer());
            if (turn.answer().failed()) {
                // the service failed during the question: show it at once; the next check turns it back on
                show(ServiceStatus.down(turn.answer().failure().service(), turn.answer().failure().reason()));
            } else {
                question.clear();
            }
            applyEnabled();
            scrollToEnd();
            refreshHistory();
        }, error -> {
            busy = false;
            messages.add(AnswerBubble.question("⚠️ Unexpected error: " + error.getMessage()));
            applyEnabled();
        });
    }

    private record Turn(RagAnswer answer, String sessionId) {
    }

    // ------------------------------------------------------------------ live progress: 🗄️ → ⚡ → 🧠

    private static String label(RagService.Stage stage) {
        return switch (stage) {
            case SEARCH -> "🗄️ Weaviate · search";
            case SYSTEM1 -> "⚡ System 1 · check passages";
            case SYSTEM2 -> "🧠 System 2 · write answer";
        };
    }

    private void resetSteps() {
        steps.forEach((stage, chip) -> setStep(chip, "rd-step-waiting", "⬜ " + label(stage)));
    }

    /** Stages before the current one are done, the current one runs, the rest wait. */
    private void showStep(RagService.Progress p) {
        steps.forEach((stage, chip) -> {
            int c = stage.compareTo(p.stage());
            if (c < 0) setStep(chip, "rd-step-done", "✅ " + label(stage));
            else if (c == 0) setStep(chip, "rd-step-active", "⏳ " + label(stage) + " — " + p.detail() + "…");
            else setStep(chip, "rd-step-waiting", "⬜ " + label(stage));
        });
    }

    /** After the answer: all ✅, or ⛔ on the stage whose service failed (later stages never ran). */
    private void finishSteps(RagAnswer answer) {
        RagService.Stage failed = !answer.failed() ? null : switch (answer.failure().service()) {
            case WEAVIATE -> RagService.Stage.SEARCH;
            case SYSTEM1 -> RagService.Stage.SYSTEM1;
            case SYSTEM2 -> RagService.Stage.SYSTEM2;
        };
        boolean noSystemTwo = !answer.failed() && answer.model() == null;   // nothing was kept: System 2 not called
        steps.forEach((stage, chip) -> {
            if (failed != null && stage == failed) setStep(chip, "rd-step-failed", "⛔ " + label(stage));
            else if (failed != null && stage.compareTo(failed) > 0) setStep(chip, "rd-step-waiting", "⬜ " + label(stage));
            else if (noSystemTwo && stage == RagService.Stage.SYSTEM2) setStep(chip, "rd-step-waiting", "➖ " + label(stage) + " — not needed");
            else setStep(chip, "rd-step-done", "✅ " + label(stage));
        });
    }

    private static void setStep(Span chip, String state, String text) {
        chip.getElement().getClassList().removeIf(c -> c.startsWith("rd-step-"));
        chip.addClassName(state);
        chip.setText(text);
    }

    private void startNewChat() {
        sessionId = null;
        showEmptyConversation();
        sessions.deselectAll();
        question.focus();
    }

    private void loadConversation(ChatSession s) {
        sessionId = s.getId();
        messages.removeAll();
        addMessages(messages, history.messages(s.getId()));
        sessions.deselectAll();
        scrollToEnd();
    }

    private void openConversationDialog(ChatSession s) {
        Div content = new Div();
        content.addClassNames("rd-messages-dialog", "flex", "flex-col", "gap-3");
        addMessages(content, history.messages(s.getId()));
        new AppDialog("🔍 " + s.getTitle(), content).open();
    }

    private static void addMessages(Div target, List<ChatMessage> list) {
        for (ChatMessage m : list) {
            if (m.getSender() == ChatMessage.Sender.USER) {
                target.add(AnswerBubble.question(m.getText()));
            } else {
                RagAnswer saved = ChatHistoryService.answerOf(m);
                target.add(saved != null ? AnswerBubble.answer(saved) : AnswerBubble.question(m.getText()));
            }
        }
    }

    private void showEmptyConversation() {
        messages.removeAll();
        Span empty = new Span("No messages yet. Ask a question below, or pick a conversation in 📂 History.");
        empty.addClassNames("rd-muted", "text-sm");
        messages.add(empty);
    }

    private void refreshHistory() {
        sessions.getDataProvider().refreshAll();
        sessionCount.setText("Conversations: " + history.countSessions());
    }

    private void updateCounter() {
        counter.setText(String.format("%,d / %,d", question.getValue().length(), MAX_QUESTION));
    }

    private void scrollToEnd() {
        messages.getElement().executeJs("requestAnimationFrame(() => this.scrollTop = this.scrollHeight)");
    }

    private void toggleFullScreen() {
        if (historyCard.hasClassName("rd-fullscreen")) {
            historyCard.removeClassName("rd-fullscreen");
        } else {
            historyCard.addClassName("rd-fullscreen");
        }
    }
}
