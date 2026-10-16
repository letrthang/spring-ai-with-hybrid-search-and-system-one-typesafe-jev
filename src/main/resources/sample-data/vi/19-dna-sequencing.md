# Công nghệ sinh học cơ bản — giải trình tự DNA

## Giải trình tự DNA là gì
Giải trình tự DNA là đọc chính xác thứ tự các base A, T, G và C trong một đoạn DNA. Nó được dùng để nhận diện sinh
vật, kiểm tra gen đã tạo dòng, tìm đột biến gây bệnh và nghiên cứu toàn bộ hệ gen. Hệ gen người đầu tiên mất khoảng
13 năm mới hoàn thành, còn ngày nay một hệ gen có thể được đọc trong khoảng một ngày.

## Giải trình tự Sanger
Giải trình tự Sanger đọc từng đoạn DNA một bằng các base kết thúc chuỗi. Một polymerase sao chép khuôn, và các base
đặc biệt dideoxy (ddNTP), mỗi loại gắn một thuốc nhuộm màu khác nhau, dừng mạch mới ở các vị trí ngẫu nhiên. Các đoạn
được sắp xếp theo kích thước, và màu ở mỗi độ dài cho biết trình tự.

## Độ dài đọc và ứng dụng của phương pháp Sanger
Một phản ứng Sanger cho một lần đọc chất lượng cao dài khoảng 700–1.000 base. Phương pháp này rất chính xác, trên
99,9% mỗi base, nhưng chậm và tốn kém cho các dự án lớn. Nó vẫn là tiêu chuẩn để kiểm tra một gen đã tạo dòng, một sản
phẩm PCR hay một đột biến đã biết.

## Giải trình tự thế hệ mới
Giải trình tự thế hệ mới (NGS) đọc cùng lúc từ hàng triệu đến hàng tỷ đoạn DNA. DNA được cắt thành các đoạn ngắn,
gắn thêm adapter, rồi từng đoạn được sao chép và đọc song song. Cách làm song song quy mô lớn này đã giảm chi phí giải
trình tự một hệ gen người từ hàng tỷ đô la xuống còn vài trăm đô la.

## Giải trình tự đọc ngắn
Giải trình tự đọc ngắn tạo ra các lần đọc dài khoảng 50–300 base mỗi lần. Một phương pháp phổ biến là giải trình tự
bằng tổng hợp, trong đó các base phát huỳnh quang được thêm vào từng cái một và camera ghi lại màu của mỗi cụm sau mỗi
bước. Đọc ngắn rẻ và chính xác nhưng gặp khó với các đoạn lặp dài.

## Giải trình tự đọc dài
Giải trình tự đọc dài đọc các phân tử DNA đơn lẻ dài hàng chục nghìn base, đôi khi hơn một triệu base. Một phương pháp
kéo DNA qua một lỗ protein rất nhỏ và đọc sự thay đổi dòng điện; phương pháp khác theo dõi một polymerase đơn lẻ đang
hoạt động theo thời gian thực. Đọc dài giúp lắp ráp hệ gen và giải quyết các đoạn lặp.

## Độ sâu giải trình tự
Độ sâu giải trình tự, hay độ phủ, là số lần đọc trung bình phủ lên mỗi base của hệ gen. Hệ gen người thường được giải
trình tự ở khoảng 30x, nghĩa là mỗi base được đọc khoảng 30 lần. Độ sâu càng cao càng dễ phân biệt một đột biến thật
với một lỗi giải trình tự ngẫu nhiên.

## Điểm chất lượng
Mỗi base trong một lần đọc đi kèm một điểm chất lượng, gọi là điểm Phred, ước tính khả năng base đó bị sai. Điểm Q20
nghĩa là xác suất sai 1 trên 100, còn Q30 là 1 trên 1.000. Các base chất lượng thấp, thường ở hai đầu của lần đọc,
được cắt bỏ trước khi phân tích.

## Chuẩn bị thư viện
Chuẩn bị thư viện là biến mẫu DNA thành dạng mà máy giải trình tự đọc được. DNA được cắt thành các đoạn có kích thước
chọn trước, thường 200–500 cặp base, và các trình tự adapter ngắn được nối vào cả hai đầu. Adapter có thể mang mã vạch
(barcode) để nhiều mẫu được gộp chung và giải trình tự trong một lần chạy.

## Giải trình tự RNA
Giải trình tự RNA (RNA-seq) đo xem những gen nào đang được bật trong mẫu và bật mạnh đến mức nào. Trước tiên RNA được
sao chép thành DNA bổ sung (cDNA) bằng enzyme phiên mã ngược, rồi được giải trình tự như DNA. Số lần đọc khớp với mỗi
gen cho biết mức độ biểu hiện của gen đó.
