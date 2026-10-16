# Bản demo này hoạt động thế nào — tóm tắt về RAG

## RAG là gì
RAG (Retrieval-Augmented Generation — sinh câu trả lời có tăng cường truy xuất) nghĩa là: trước khi mô hình ngôn ngữ
trả lời, ứng dụng tìm các đoạn văn liên quan đến câu hỏi và thêm chúng vào prompt. Khi đó mô hình trả lời dựa trên các
đoạn văn này chứ không chỉ dựa vào trí nhớ, nên câu trả lời có thể dẫn nguồn và ít bị bịa ra hơn.

## Tìm kiếm lai (hybrid search)
Bản demo này tìm kiếm trong Weaviate theo hai cách. BM25 xếp hạng các đoạn văn có chung những từ hiếm với câu hỏi,
chẳng hạn tên Cas9 hay mã NGG. Tìm kiếm vector xếp hạng các đoạn văn có ý nghĩa tương tự, kể cả khi chúng dùng từ khác.
Reciprocal Rank Fusion gộp hai bảng xếp hạng: mỗi đoạn văn nhận 1 / (60 + hạng) từ mỗi danh sách có chứa nó.

## System 1 và System 2
System 1 là một mô hình ra quyết định nhỏ và nhanh (Laya). Với mỗi đoạn văn tìm được, nó trả lời các câu hỏi có kiểu
xác định bằng một xác suất: đoạn văn có liên quan không, có trả lời được câu hỏi không, có cố đưa ra chỉ thị không.
System 2 là một mô hình ngôn ngữ lớn trên OpenRouter, viết câu trả lời cuối cùng từ các đoạn văn mà System 1 giữ lại.
