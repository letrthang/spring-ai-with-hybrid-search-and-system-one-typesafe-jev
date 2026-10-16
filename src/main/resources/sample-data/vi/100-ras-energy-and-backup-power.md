# Cơ bản về RAS — Sử dụng năng lượng và nguồn điện dự phòng

## Điện năng trong RAS được dùng vào đâu
Điện năng trong RAS chủ yếu được dùng cho bơm nước, thường chiếm 30 đến 50 % tổng lượng điện. Sưởi hoặc làm mát,
máy thổi khí cho khử khí và sục khí, máy tạo oxy, đèn UV và chiếu sáng chiếm phần lớn phần còn lại. Biết được tỷ lệ
phân bổ này cho thấy nỗ lực tiết kiệm năng lượng ở đâu sẽ hoàn vốn nhanh nhất.

## Tính công suất bơm
Công suất bơm trong RAS có thể ước tính bằng công thức P = ρ · g · Q · H / η, trong đó Q là lưu lượng, H là cột áp
và η là hiệu suất bơm. Ví dụ, bơm 1.000 m³/h với cột áp 1,5 m bằng một bơm hiệu suất 70 % cần khoảng 5,8 kW. Mỗi
mét cột áp tăng thêm làm tốn thêm khoảng 3,9 kW ở lưu lượng này, nên thiết kế cột áp thấp tiết kiệm rất nhiều năng
lượng.

## Năng lượng cho oxy và máy thổi khí
Máy tạo oxy tại chỗ dùng công nghệ PSA thường cần khoảng 0,5 đến 1 kWh điện cho mỗi kilôgam oxy sản xuất ra. Máy
thổi khí cho tháp khử khí và bộ lọc sinh học giá thể chuyển động chạy suốt ngày đêm, nên máy thổi hiệu suất cao có
biến tần giúp tiết kiệm nhiều trong một năm. Giảm tổn thất áp suất khí trong đường ống và đĩa khuếch tán cũng làm
giảm công suất máy thổi.

## Đo điện riêng cho từng phụ tải
Đo điện riêng nghĩa là lắp công tơ điện riêng cho các phụ tải lớn như cụm bơm, máy thổi khí, bơm nhiệt và máy tạo
oxy. Công tơ riêng cho thấy thiết bị nào dùng nhiều điện nhất và phát hiện sự cố, chẳng hạn bơm ngốn nhiều điện hơn
khi cánh bơm bị mòn. Theo dõi số kWh trên mỗi kilôgam thức ăn hằng tuần giúp dễ dàng phát hiện sự thay đổi chậm.

## Giảm công suất đỉnh
Công suất đỉnh là mức tiêu thụ điện cao nhất tại một thời điểm, và nhiều biểu giá điện tính thêm phí cho mức này.
Các trại RAS giảm công suất đỉnh bằng cách khởi động các động cơ lớn theo từng đợt với bộ khởi động mềm hoặc biến
tần thay vì khởi động trực tiếp. Các phụ tải linh hoạt như tách nước bùn hoặc đun nóng nước có thể chuyển sang ban
đêm khi giá điện thấp hơn.

## Chọn công suất máy phát dự phòng
Máy phát điện dự phòng cho RAS phải đáp ứng đồng thời mọi phụ tải quan trọng, gồm bơm tuần hoàn, máy thổi khí, cấp
oxy, hệ điều khiển và báo động. Động cơ điện có thể kéo dòng gấp 5 đến 7 lần dòng làm việc khi khởi động, nên máy
phát thường được chọn bằng 1,25 đến 1,5 lần tải làm việc, hoặc các phụ tải được khởi động theo từng đợt. Nhiên liệu
dự trữ nên đủ cho ít nhất 24 đến 72 giờ.

## Bộ chuyển nguồn tự động
Bộ chuyển nguồn tự động (ATS) phát hiện mất điện lưới, khởi động máy phát dự phòng và chuyển phụ tải của trại sang
máy phát, thường trong vòng 10 đến 30 giây. Khi điện lưới có lại và ổn định, ATS chuyển trở về điện lưới và cho máy
phát chạy nguội. Bơm và máy thổi khí nên khởi động lại theo thứ tự cài sẵn để máy phát không bị quá tải.

## UPS cho hệ điều khiển và báo động
Bộ lưu điện (UPS) giữ cho PLC, cảm biến, mạng và bộ quay số báo động hoạt động trong khoảng trống trước khi máy
phát tiếp quản. UPS cũng nên duy trì các hệ thống này ít nhất 30 đến 60 phút phòng khi máy phát không khởi động
được. Không có UPS, trại sẽ mất báo động và dữ liệu đúng vào lúc cần chúng nhất.

## Oxy giảm nhanh thế nào khi mất điện
Oxy hòa tan trong bể RAS giảm rất nhanh khi mất điện, vì dòng nước và nguồn cấp oxy ngừng trong khi cá vẫn tiếp tục
hô hấp. Cá ở mật độ 50 kg/m³ dùng 300 mg oxy mỗi kg mỗi giờ sẽ tiêu thụ 15 mg/L mỗi giờ, nên oxy có thể giảm từ 10
xuống 5 mg/L trong khoảng 20 phút. Đó là lý do oxy dự phòng cấp từ bồn oxy lỏng có áp phải hoạt động được mà không
cần điện.

## Bảo dưỡng máy phát và nhiên liệu
Máy phát dự phòng của RAS cần được bảo dưỡng định kỳ, gồm khởi động hằng tuần, chạy với tải thật hằng tháng và kiểm
tra dầu, nước làm mát và ắc quy khởi động. Dầu diesel lưu trữ nhiều tháng có thể bị vi sinh vật phát triển và đọng
nước, nên cần xả nước khỏi bồn và kiểm tra hoặc thay nhiên liệu. Bồn nhiên liệu đầy và hợp đồng giao nhiên liệu
khẩn cấp giúp bảo vệ trại khi mất điện kéo dài.
