import { LitElement, css, html, nothing } from 'lit';
import { customElement, property } from 'lit/decorators.js';

/**
 * <rd-system-map> — "How it works": the RAG pipeline as one live diagram (💬 Chat page).
 *
 *   ♻️ learned answers? (reuse if ⚡ agrees)
 *         ↑
 *   💬 Question → 🗄️ Weaviate → (RRF) → 📄 Top passages → ⚡ System 1 → 🧠 System 2 → ✅ Answer
 *                                                            ╰┈┈→ 🚫 nothing kept     ↓
 *                                                                          ⚡ verify → 🧠 learned answers · N
 *
 * The server (SystemMap.java) sets three properties:
 *   labels  every text, already translated
 *   states  WEAVIATE / SYSTEM1 / SYSTEM2 → READY | WAITING | DOWN | UNKNOWN
 *   tips    the same keys → the full status sentence (tooltip)
 *   learned how many answers are learned (-1 = unknown)
 * Clicking an ℹ️ fires `rd-hint` with { hint } so the server opens that lesson.
 *
 * Connectors: a dashed track that flows toward the arrow, plus a glowing particle; particles are staggered so the
 * flow visibly runs left → right. A connector into a service that is down turns red and stops. Each stage has its
 * own accent colour; text keeps WCAG AA contrast in light and dark mode. Column layout on a phone; no motion for
 * people who ask for reduced motion.
 */
type State = 'READY' | 'WAITING' | 'DOWN' | 'UNKNOWN';

interface Stage {
  id: string;
  icon: string;
  name: string;
  sub: string;
  accent: string;
  hint: string;
  service?: 'WEAVIATE' | 'SYSTEM1' | 'SYSTEM2';
}

@customElement('rd-system-map')
export class RdSystemMap extends LitElement {
  @property({ type: Object }) labels: Record<string, string> = {};
  @property({ type: Object }) states: Record<string, State> = {};
  @property({ type: Object }) tips: Record<string, string> = {};
  @property({ type: Number }) learned = -1;

  private t(key: string): string {
    return this.labels[key] ?? key;
  }

  private stages(): Stage[] {
    return [
      { id: 'question', icon: '💬', name: this.t('question'), sub: this.t('questionSub'), accent: '--rd-indigo', hint: 'RAG' },
      { id: 'weaviate', icon: '🗄️', name: this.t('weaviate'), sub: this.t('weaviateSub'), accent: '--rd-teal', hint: 'WEAVIATE', service: 'WEAVIATE' },
      { id: 'passages', icon: '📄', name: this.t('passages'), sub: this.t('passagesSub'), accent: '--rd-sky', hint: 'CHUNKS' },
      { id: 'system1', icon: '⚡', name: this.t('system1'), sub: this.t('system1Sub'), accent: '--rd-amber', hint: 'SYSTEM1', service: 'SYSTEM1' },
      { id: 'system2', icon: '🧠', name: this.t('system2'), sub: this.t('system2Sub'), accent: '--rd-violet', hint: 'SYSTEM2', service: 'SYSTEM2' },
      { id: 'answer', icon: '✅', name: this.t('answer'), sub: this.t('answerSub'), accent: '--rd-emerald', hint: 'SOURCES' },
    ];
  }

  private state(s: Stage): State {
    return s.service ? (this.states[s.service] ?? 'UNKNOWN') : 'READY';
  }

  private hint(name: string, e?: Event) {
    e?.stopPropagation();
    this.dispatchEvent(new CustomEvent('rd-hint', { detail: { hint: name }, bubbles: true, composed: true }));
  }

  private info(name: string) {
    return html`<button class="info" title=${this.t('whatIsThis')} aria-label=${this.t('whatIsThis')}
        @click=${(e: Event) => this.hint(name, e)}>i</button>`;
  }

  private statusPill(s: Stage) {
    if (!s.service) return nothing;
    const st = this.state(s);
    const text = st === 'READY' ? this.t('online') : st === 'DOWN' ? this.t('offline')
        : st === 'WAITING' ? this.t('waiting') : this.t('checking');
    return html`<span class="pill ${st}" title=${this.tips[s.service] ?? ''}><i></i>${text}</span>`;
  }

  private node(s: Stage) {
    return html`
      <div class="node ${this.state(s)} ${s.id}" style="--accent: var(${s.accent})">
        <div class="icon">${s.icon}</div>
        <div class="name">${s.name}</div>
        <div class="sub">${s.sub}</div>
        ${this.statusPill(s)}
        ${this.info(s.hint)}
      </div>`;
  }

  /** A connector from one stage to the next; its state is the state of the stage it leads into. */
  private link(from: Stage, to: Stage, index: number, label?: string, hint?: string) {
    const st = this.state(to);
    return html`
      <div class="link ${st} ${label ? 'labelled' : ''}" style="--from: var(${from.accent}); --to: var(${to.accent}); --delay: ${index * 0.32}s">
        <div class="track"></div>
        <div class="particle"></div>
        <div class="arrow"></div>
        ${label ? html`<span class="label" @click=${() => hint && this.hint(hint)}>${label}<b>ℹ</b></span>` : nothing}
      </div>`;
  }

  render() {
    const [q, w, p, s1, s2, a] = this.stages();
    const store = this.states['WEAVIATE'] ?? 'UNKNOWN';   // the learned answers live in Weaviate
    const count = this.learned >= 0 ? ' · ' + this.t('learnedCount').replace('{0}', String(this.learned)) : '';
    return html`
      <section class="card">
        <header>
          <h3>${this.t('title')}</h3>
          ${this.info('DATA_FLOW')}
        </header>
        <p class="lead">${this.t('lead')}</p>
        <div class="flow">
          <div class="column">
            <div class="branch top ${store}">
              <button class="decision reuse" @click=${() => this.hint('LEARNING')}>♻️ ${this.t('reuse')} <b>ℹ</b></button>
              <div class="rise"></div>
            </div>
            ${this.node(q)}
          </div>
          ${this.link(q, w, 0)}
          ${this.node(w)} ${this.link(w, p, 1, this.t('rrf'), 'RRF')}
          ${this.node(p)} ${this.link(p, s1, 2)}
          <div class="column">
            ${this.node(s1)}
            <div class="branch ${this.state(s1)}">
              <div class="drop"></div>
              <span class="decision">🚫 ${this.t('decision')}</span>
            </div>
          </div>
          ${this.link(s1, s2, 3)}
          ${this.node(s2)} ${this.link(s2, a, 4)}
          <div class="column last">
            ${this.node(a)}
            <div class="branch ${store}">
              <div class="drop save"></div>
              <button class="decision learned" @click=${() => this.hint('LEARNING')}>⚡ ${this.t('verify')} → 🧠 ${this.t('learnedStore')}${count} <b>ℹ</b></button>
            </div>
          </div>
        </div>
        <footer>
          <span><i class="READY"></i>${this.t('online')}</span>
          <span><i class="WAITING"></i>${this.t('waiting')}</span>
          <span><i class="DOWN"></i>${this.t('offline')}</span>
        </footer>
      </section>`;
  }

  static styles = css`
    :host {
      display: block;
      --rd-indigo: light-dark(#4f46e5, #818cf8);
      --rd-teal: light-dark(#0d9488, #2dd4bf);
      --rd-sky: light-dark(#0284c7, #38bdf8);
      --rd-amber: light-dark(#d97706, #fbbf24);
      --rd-violet: light-dark(#7c3aed, #a78bfa);
      --rd-emerald: light-dark(#059669, #34d399);
      --rd-red: light-dark(#dc2626, #f87171);
      --rd-text: light-dark(#0f172a, #f1f5f9);
      /* text on tinted pills: darker than the accents, ≥ 5:1 contrast in light mode (WCAG AA) */
      --rd-ok-text: light-dark(#047857, #6ee7b7);
      --rd-warn-text: light-dark(#b45309, #fcd34d);
      --rd-bad-text: light-dark(#b91c1c, #fca5a5);
      --rd-learn-text: light-dark(#3730a3, #c7d2fe);
      --rd-muted: light-dark(#475569, #cbd5e1);
      --rd-surface: light-dark(#ffffff, #151a24);
      --rd-line: light-dark(#e2e8f0, #2b3443);
      color: var(--rd-text);
    }
    *, *::before, *::after { box-sizing: border-box; }
    .card {
      border-radius: 16px;
      border: 1px solid var(--rd-line);
      background:
        radial-gradient(120% 140% at 0% 0%, light-dark(rgba(99, 102, 241, .07), rgba(129, 140, 248, .10)), transparent 55%),
        radial-gradient(120% 140% at 100% 100%, light-dark(rgba(16, 185, 129, .07), rgba(52, 211, 153, .08)), transparent 55%),
        var(--rd-surface);
      padding: 16px 18px 12px;
      container-type: inline-size;
      box-shadow: 0 1px 2px light-dark(rgba(15, 36, 56, .06), rgba(0, 0, 0, .3));
    }
    header { display: flex; align-items: center; gap: 6px; }
    h3 { margin: 0; font-size: 1rem; }
    .lead { margin: 4px 0 0; font-size: .85rem; color: var(--rd-muted); }

    .flow { display: flex; align-items: center; padding: 84px 2px 78px; overflow-x: auto; }

    /* ---- a stage */
    .node {
      position: relative;
      flex: 0 0 auto;
      width: 112px;
      padding: 12px 8px 10px;
      border-radius: 14px;
      text-align: center;
      background: var(--rd-surface);
      border: 1.5px solid color-mix(in srgb, var(--accent) 45%, var(--rd-line));
      box-shadow: 0 6px 18px -8px color-mix(in srgb, var(--accent) 55%, transparent),
                  inset 0 3px 0 var(--accent);
      transition: transform .25s ease, box-shadow .25s ease, border-color .3s ease;
    }
    .node:hover { transform: translateY(-2px); box-shadow: 0 10px 24px -8px color-mix(in srgb, var(--accent) 70%, transparent), inset 0 3px 0 var(--accent); }
    .icon {
      width: 38px; height: 38px; margin: 0 auto 6px;
      display: grid; place-items: center; font-size: 20px;
      border-radius: 12px;
      background: color-mix(in srgb, var(--accent) 16%, transparent);
    }
    .name { font-weight: 700; font-size: .84rem; line-height: 1.25; }
    .sub { margin-top: 2px; font-size: .72rem; line-height: 1.3; color: var(--rd-muted); }
    .weaviate .icon { border-radius: 50% / 38%; }   /* a database */
    .node.DOWN { border-color: var(--rd-red); box-shadow: 0 6px 18px -8px color-mix(in srgb, var(--rd-red) 60%, transparent), inset 0 3px 0 var(--rd-red); }

    .pill {
      display: inline-flex; align-items: center; gap: 5px;
      margin-top: 8px; padding: 2px 8px;
      border-radius: 999px; font-size: .68rem; font-weight: 600;
      border: 1px solid currentColor;
    }
    .pill i, footer i { width: 8px; height: 8px; border-radius: 50%; background: currentColor; display: inline-block; }
    .pill.READY { color: var(--rd-ok-text); background: color-mix(in srgb, var(--rd-emerald) 12%, transparent); }
    .pill.READY i { animation: pulse 2s ease-out infinite; }
    .pill.WAITING { color: var(--rd-warn-text); background: color-mix(in srgb, var(--rd-amber) 12%, transparent); }
    .pill.DOWN { color: var(--rd-bad-text); background: color-mix(in srgb, var(--rd-red) 12%, transparent); }
    .pill.UNKNOWN { color: var(--rd-muted); }

    .info {
      position: absolute; top: 6px; right: 6px;
      width: 18px; height: 18px; padding: 0;
      border-radius: 50%; border: 1px solid var(--rd-line);
      background: var(--rd-surface); color: var(--rd-muted);
      font: italic 700 11px/1 Georgia, serif; cursor: pointer;
    }
    header .info { position: static; }
    .info:hover, .info:focus-visible { color: var(--rd-text); border-color: var(--rd-violet); outline: none; }

    /* ---- a connector: dashed track flowing → , a glowing particle, an arrowhead */
    .link { position: relative; flex: 1 1 36px; min-width: 28px; height: 24px; }
    .link.labelled { flex: 2 1 100px; min-width: 100px; }   /* room for "merge with RRF" above the line */
    .track {
      position: absolute; left: 0; right: 8px; top: 50%; height: 3px; transform: translateY(-50%);
      border-radius: 3px;
      background:
        repeating-linear-gradient(90deg, rgba(255, 255, 255, .0) 0 6px, var(--rd-surface) 6px 10px),
        linear-gradient(90deg, var(--from), var(--to));
      background-size: 10px 100%, 100% 100%;
      animation: dash .6s linear infinite;
    }
    .particle {
      position: absolute; top: 50%; left: 0;
      width: 9px; height: 9px; margin-top: -4.5px; border-radius: 50%;
      background: var(--to);
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--to) 25%, transparent), 0 0 12px var(--to);
      animation: travel 2s cubic-bezier(.45, 0, .55, 1) var(--delay) infinite;
      opacity: 0;
    }
    .arrow {
      position: absolute; right: 0; top: 50%; transform: translateY(-50%);
      border-left: 9px solid var(--to); border-top: 6px solid transparent; border-bottom: 6px solid transparent;
    }
    .label {
      position: absolute; bottom: calc(50% + 9px); left: 50%; transform: translateX(-50%);
      white-space: nowrap; cursor: pointer;
      font-size: .7rem; font-weight: 600; padding: 2px 8px; border-radius: 999px;
      color: var(--rd-text);
      background: color-mix(in srgb, var(--to) 14%, var(--rd-surface));
      border: 1px solid color-mix(in srgb, var(--to) 40%, transparent);
    }
    .label b { margin-left: 4px; font: italic 700 10px Georgia, serif; color: var(--rd-muted); }
    .link.WAITING .track { animation-duration: 1.6s; }
    .link.WAITING .particle { animation-duration: 3.5s; }
    .link.DOWN .track { animation: none; background: repeating-linear-gradient(90deg, var(--rd-red) 0 6px, transparent 6px 10px); }
    .link.DOWN .particle { display: none; }
    .link.DOWN .arrow { border-left-color: var(--rd-red); }
    .link.UNKNOWN .particle { display: none; }

    /* ---- System 1's decision branch */
    .column { position: relative; flex: 0 0 auto; }
    .branch { position: absolute; top: 100%; left: 50%; transform: translateX(-50%); display: flex; flex-direction: column; align-items: center; }
    .drop {
      width: 3px; height: 24px; position: relative;
      background: repeating-linear-gradient(180deg, var(--rd-red) 0 5px, transparent 5px 9px);
      background-size: 100% 9px;
      animation: drop .7s linear infinite;
    }
    .drop::after {
      content: ""; position: absolute; left: 50%; bottom: -6px; transform: translateX(-50%);
      border-top: 8px solid var(--rd-red); border-left: 5px solid transparent; border-right: 5px solid transparent;
    }
    .decision {
      margin-top: 8px; white-space: nowrap;
      font-size: .7rem; font-weight: 600; padding: 3px 9px; border-radius: 999px;
      color: var(--rd-bad-text);
      background: color-mix(in srgb, var(--rd-red) 10%, var(--rd-surface));
      border: 1px dashed var(--rd-red);
    }
    .branch.DOWN .drop, .branch.UNKNOWN .drop { animation: none; }

    /* self-learning: ♻️ the lookup above the question (line goes up), ⚡→🧠 the save-back below the answer */
    .branch.top { top: auto; bottom: 100%; left: 0; transform: none; align-items: flex-start; }
    .branch.top .rise { margin-left: 54px; }                       /* under the pill, over the question's centre */
    .column.last .branch { left: auto; right: 0; transform: none; align-items: flex-end; }
    .column.last .drop.save { margin-right: 54px; }               /* the answer is the rightmost node: keep inside */
    .rise {
      width: 3px; height: 24px; position: relative; margin-top: 8px;
      background: repeating-linear-gradient(0deg, var(--rd-indigo) 0 5px, transparent 5px 9px);
      background-size: 100% 9px;
      animation: rise .7s linear infinite;
    }
    .rise::after {
      content: ""; position: absolute; left: 50%; top: -6px; transform: translateX(-50%);
      border-bottom: 8px solid var(--rd-indigo); border-left: 5px solid transparent; border-right: 5px solid transparent;
    }
    .drop.save { background: repeating-linear-gradient(180deg, var(--rd-emerald) 0 5px, transparent 5px 9px); background-size: 100% 9px; }
    .drop.save::after { border-top-color: var(--rd-emerald); }
    button.decision { font: inherit; font-size: .7rem; font-weight: 600; cursor: pointer; }
    button.decision b { margin-left: 4px; font: italic 700 10px Georgia, serif; color: var(--rd-muted); }
    .decision.reuse {
      margin-top: 0; color: var(--rd-learn-text);
      background: color-mix(in srgb, var(--rd-indigo) 10%, var(--rd-surface)); border: 1px dashed var(--rd-indigo);
    }
    .decision.learned {
      color: var(--rd-ok-text);
      background: color-mix(in srgb, var(--rd-emerald) 10%, var(--rd-surface)); border: 1px dashed var(--rd-emerald);
    }
    .branch.DOWN .rise, .branch.UNKNOWN .rise, .branch.DOWN .drop.save, .branch.UNKNOWN .drop.save { animation: none; }

    footer { display: flex; justify-content: flex-end; gap: 14px; font-size: .72rem; color: var(--rd-muted); }
    footer span { display: inline-flex; align-items: center; gap: 5px; }
    footer i.READY { background: var(--rd-emerald); }
    footer i.WAITING { background: var(--rd-amber); }
    footer i.DOWN { background: var(--rd-red); }

    @keyframes dash { to { background-position: 10px 0, 0 0; } }
    @keyframes drop { to { background-position: 0 9px; } }
    @keyframes rise { to { background-position: 0 -9px; } }
    @keyframes dash-down { to { background-position: 0 10px, 0 0; } }
    @keyframes travel-down {
      0% { top: 0; opacity: 0; }
      15% { opacity: 1; }
      85% { opacity: 1; }
      100% { top: calc(100% - 14px); opacity: 0; }
    }
    @keyframes travel {
      0% { left: 0; opacity: 0; transform: scale(.6); }
      12% { opacity: 1; transform: scale(1); }
      85% { opacity: 1; }
      100% { left: calc(100% - 14px); opacity: 0; transform: scale(.6); }
    }
    @keyframes pulse {
      0% { box-shadow: 0 0 0 0 color-mix(in srgb, var(--rd-emerald) 70%, transparent); }
      100% { box-shadow: 0 0 0 7px transparent; }
    }

    /* top to bottom whenever the card is narrower than the row needs (phone, or a laptop with the menu open) */
    @container (max-width: 900px) {
      .flow { flex-direction: column; align-items: stretch; padding: 8px 0; }
      /* each stage becomes one compact row: icon | name + sub | status */
      .node {
        width: auto;
        display: grid;
        grid-template-columns: 36px 1fr auto;
        grid-template-areas: "icon name pill" "icon sub pill";
        align-items: center;
        column-gap: 10px;
        text-align: left;
        padding: 8px 34px 8px 10px;
      }
      .icon { grid-area: icon; width: 36px; height: 36px; margin: 0; font-size: 18px; }
      .name { grid-area: name; }
      .sub { grid-area: sub; margin-top: 0; }
      .pill { grid-area: pill; margin-top: 0; }
      .info { top: 50%; transform: translateY(-50%); }
      .node:hover { transform: none; }
      .column { display: flex; flex-direction: column; align-items: stretch; }
      .branch { position: static; transform: none; flex-direction: row; justify-content: center; margin-top: 8px; }
      .drop, .rise { display: none; }
      .branch.top { order: 2; }   /* stacked: the reuse note sits under the question, like the others */
      .branch.top, .column.last .branch { align-items: center; }
      .decision { margin-top: 0; }
      .link, .link.labelled { flex: 0 0 30px; min-width: 0; height: auto; }
      .track {
        left: 50%; right: auto; top: 0; bottom: 8px; width: 3px; height: auto; transform: translateX(-50%);
        background:
          repeating-linear-gradient(180deg, rgba(255, 255, 255, 0) 0 6px, var(--rd-surface) 6px 10px),
          linear-gradient(180deg, var(--from), var(--to));
        background-size: 100% 10px, 100% 100%;
        animation-name: dash-down;
      }
      .particle { left: 50%; top: 0; margin: 0 0 0 -4.5px; animation-name: travel-down; }
      .arrow {
        right: auto; left: 50%; top: auto; bottom: 0; transform: translateX(-50%);
        border: none; border-top: 9px solid var(--to); border-left: 6px solid transparent; border-right: 6px solid transparent;
      }
      .link.DOWN .arrow { border-top-color: var(--rd-red); }
      .link.DOWN .track { background: repeating-linear-gradient(180deg, var(--rd-red) 0 6px, transparent 6px 10px); }
      .label { bottom: auto; top: 50%; left: calc(50% + 14px); transform: translateY(-50%); }
    }
    @media (prefers-reduced-motion: reduce) {
      .track, .drop, .rise, .pill.READY i { animation: none; }
      .particle { display: none; }
      .node { transition: none; }
    }
  `;
}

declare global {
  interface HTMLElementTagNameMap {
    'rd-system-map': RdSystemMap;
  }
}
