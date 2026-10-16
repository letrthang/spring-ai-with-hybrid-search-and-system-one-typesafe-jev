# Công nghệ sinh học cơ bản — đo dòng tế bào (flow cytometry)

## Đo dòng tế bào là gì
Đo dòng tế bào (flow cytometry) là phương pháp đo hàng nghìn tế bào riêng lẻ mỗi giây khi chúng lần lượt đi qua một
chùm tia laser. Với mỗi tế bào, máy đo dòng tế bào ghi lại lượng ánh sáng mà tế bào tán xạ và lượng huỳnh quang mà tế
bào phát ra. Một lần chạy điển hình phân tích từ 10.000 đến 1.000.000 tế bào trong vài phút và cho số liệu thống kê
về kích thước, độ phức tạp bên trong và các dấu ấn được đánh dấu của tế bào.

## Hội tụ thủy động
Hội tụ thủy động là cách máy đo dòng tế bào xếp các tế bào thành một hàng đơn. Dòng mẫu được bơm vào giữa một dòng
dịch bao (sheath fluid) chảy nhanh hơn, dòng dịch bao ép mẫu thành một lõi hẹp chỉ rộng vài micromet. Vì các tế bào
đi qua điểm laser từng cái một, mỗi xung ánh sáng thuộc về đúng một tế bào.

## Tán xạ thẳng và tán xạ bên
Tán xạ thẳng (FSC) và tán xạ bên (SSC) là hai tín hiệu tán xạ ánh sáng mà máy đo dòng tế bào đo được mà không cần
nhuộm. FSC là ánh sáng bị lệch một góc nhỏ dọc theo đường đi của tia laser và phản ánh gần đúng kích thước tế bào.
SSC là ánh sáng bị hắt ra ở góc khoảng 90 độ và phản ánh độ phức tạp bên trong như các hạt, vì vậy bạch cầu trung
tính có SSC cao còn tế bào lympho có SSC thấp.

## Chất đánh dấu huỳnh quang trong đo dòng tế bào
Chất đánh dấu huỳnh quang giúp đo dòng tế bào đo được các phân tử cụ thể trên bề mặt hoặc bên trong tế bào. Kháng thể
gắn với chất nhuộm như FITC hoặc PE bám vào các dấu ấn bề mặt như CD4 hay CD8, còn chất nhuộm DNA như propidium
iodide đo hàm lượng DNA hoặc đánh dấu tế bào chết. Mỗi chất nhuộm hấp thụ ánh sáng từ một tia laser và phát ra ở bước
sóng dài hơn; ví dụ FITC được kích thích ở 488 nm và phát xạ gần 520 nm.

## Laser, kính lọc và đầu dò
Máy đo dòng tế bào tách các màu bằng laser, kính lọc quang học và đầu dò ánh sáng. Các máy thông dụng có từ hai đến
năm tia laser, ví dụ ở 405, 488 và 640 nm, và gương lưỡng sắc cùng kính lọc dải thông đưa mỗi dải màu đến một ống
nhân quang (photomultiplier tube) riêng. Các máy phân tích hiện đại có thể đo cùng lúc 20 thông số huỳnh quang trở
lên trên mỗi tế bào.

## Khoanh vùng (gating)
Khoanh vùng (gating) là bước trong phân tích đo dòng tế bào, khi người dùng vẽ các vùng trên biểu đồ để chọn một quần
thể tế bào. Vùng đầu tiên trên biểu đồ FSC theo SSC loại bỏ mảnh vụn, vùng thứ hai loại bỏ các cặp đôi (doublet) khi
hai tế bào dính vào nhau, và các vùng sau chọn những tế bào dương tính với dấu ấn mong muốn. Mỗi kết quả được báo cáo
theo phần trăm của quần thể mẹ, ví dụ 45 % tế bào T dương tính CD4 trong số tế bào lympho.

## Bù trừ (compensation)
Bù trừ (compensation) là việc hiệu chỉnh sự chồng lấn phổ giữa các chất nhuộm huỳnh quang trong đo dòng tế bào. Phổ
phát xạ của chất nhuộm khá rộng nên một phần ánh sáng FITC tràn sang đầu dò PE, và nếu không hiệu chỉnh, tế bào có
thể trông như dương tính kép giả. Người ta chạy các mẫu đối chứng chỉ nhuộm một màu để đo mức tràn của từng chất
nhuộm, rồi phần mềm trừ phần đó khỏi các kênh khác.

## Đối chứng trong đo dòng tế bào
Thí nghiệm đo dòng tế bào cần các mẫu đối chứng để xác định nơi bắt đầu tín hiệu dương tính. Mẫu không nhuộm cho thấy
mức tự phát huỳnh quang tự nhiên của tế bào, còn đối chứng FMO (fluorescence-minus-one), chứa mọi chất nhuộm trừ một,
giúp đặt ranh giới vùng cho màu bị thiếu đó. Chất nhuộm đánh giá sống chết và đối chứng isotype giúp tách tín hiệu
thật khỏi tế bào chết và sự gắn kháng thể không đặc hiệu.

## Phân loại tế bào (FACS)
Phân loại tế bào kích hoạt bằng huỳnh quang, hay FACS, là một dạng đo dòng tế bào tách tế bào ra các ống riêng dựa
trên tín hiệu của chúng. Dòng chất lỏng được rung để vỡ thành những giọt rất nhỏ, mỗi giọt chứa nhiều nhất một tế
bào, và giọt chứa tế bào cần lấy được tích điện rồi bị các bản điện cực làm lệch hướng vào ống thu. Máy phân loại tế
bào có thể xử lý hàng chục nghìn tế bào mỗi giây và đạt độ tinh sạch trên 95 %.

## Ứng dụng phổ biến của đo dòng tế bào
Đo dòng tế bào được dùng rộng rãi trong bệnh viện và phòng thí nghiệm nghiên cứu. Phòng xét nghiệm lâm sàng đếm tế
bào T CD4 để theo dõi nhiễm HIV, chẩn đoán bệnh bạch cầu và u lympho dựa trên dấu ấn bề mặt, và đếm tế bào gốc dương
tính CD34 trước khi ghép. Trong nghiên cứu, đo dòng tế bào đo chu kỳ tế bào, phát hiện chết theo chương trình
(apoptosis) bằng nhuộm annexin V và kiểm tra hiệu suất chuyển nhiễm bằng gen báo cáo GFP.
