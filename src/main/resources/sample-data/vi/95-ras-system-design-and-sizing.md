# Cơ bản về RAS — Thiết kế và tính toán quy mô hệ thống

## Thiết kế RAS bắt đầu từ đâu
Thiết kế RAS bắt đầu từ mục tiêu sản xuất: bao nhiêu tấn cá mỗi năm, ở kích cỡ thu hoạch nào và với thời gian nuôi
bao lâu. Ba con số này cho biết sinh khối tồn tại tối đa mà trại phải chứa tại bất kỳ thời điểm nào, và từ đó quyết
định kích thước của mọi bể, bơm và bộ lọc. Ví dụ, một trại nuôi tối đa 40 tấn cá được thiết kế theo mức đỉnh 40 tấn
đó, chứ không theo lượng cá trung bình.

## Thể tích bể tính từ sinh khối và mật độ
Thể tích bể cá của một hệ RAS bằng sinh khối tối đa chia cho mật độ thả nuôi thiết kế. Nuôi 40000 kg cá hồi vân ở
50 kg/m³ cần 800 m³ nước trong bể. Người thiết kế thường thêm 10 đến 20 % thể tích bể dự phòng cho việc phân cỡ,
cách ly và các lô thu hoạch, vì cá hiếm khi được phân bố hoàn toàn đều giữa các bể.

## Tải lượng thức ăn thiết kế
Tải lượng thức ăn thiết kế của RAS là lượng thức ăn lớn nhất mà hệ thống phải xử lý mỗi ngày, tính bằng sinh khối
đỉnh nhân với tỷ lệ cho ăn. Đàn cá 40 tấn được cho ăn 1 % khối lượng cơ thể mỗi ngày sẽ ăn 400 kg thức ăn mỗi ngày.
Vì lượng oxy tiêu thụ, CO2, amoniac và chất rắn đều tăng theo lượng thức ăn, con số duy nhất này quyết định kích
thước của hệ thống oxy, tháp khử khí, bộ lọc sinh học và lọc trống.

## Nhu cầu oxy
Nhu cầu oxy của RAS được ước tính từ tải lượng thức ăn: cá dùng khoảng 0.3 đến 0.5 kg oxy cho mỗi kg thức ăn, và bộ
lọc sinh học dùng thêm khoảng 0.1 đến 0.15 kg. Với 400 kg thức ăn mỗi ngày, hệ thống phải cung cấp khoảng 160 đến
260 kg oxy mỗi ngày. Nguồn oxy, như oxy lỏng hoặc máy tạo oxy, cần đáp ứng được các giờ cao điểm sau khi cho ăn và
có biên an toàn.

## Lưu lượng tính từ cân bằng oxy
Lưu lượng tuần hoàn của RAS thường được tính bằng lượng oxy cá cần chia cho lượng oxy mà mỗi mét khối nước có thể
mang đến. Nếu cá tiêu thụ 160 kg oxy mỗi ngày, khoảng 6.7 kg mỗi giờ, và nước vào bể ở 15 mg/L rồi ra ở 8 mg/L, thì
mỗi mét khối nước mang đến 7 g. Lưu lượng cần thiết khi đó khoảng 950 m³/h, đủ thay toàn bộ 800 m³ nước bể khoảng
mỗi 50 phút.

## Tính kích thước bộ lọc sinh học
Bộ lọc sinh học của RAS được tính theo tải lượng amoniac hằng ngày, thường ước tính bằng TAN = thức ăn × tỷ lệ đạm
× 0.092. Với 400 kg thức ăn chứa 45 % đạm, cá thải ra khoảng 16.6 kg TAN mỗi ngày. Ở tốc độ loại bỏ 0.4 g TAN trên
mỗi m² mỗi ngày, giá thể cần khoảng 41500 m² bề mặt, tức khoảng 83 m³ giá thể lọc tầng chuyển động ở 500 m²/m³.

## Tính kích thước tháp khử khí
Tháp khử CO2 được thiết kế để xử lý phần lớn hoặc toàn bộ lưu lượng tuần hoàn với đủ không khí để giữ CO2 trong bể
dưới khoảng 15 đến 20 mg/L. Cá tạo ra khoảng 1.4 kg CO2 cho mỗi kg oxy tiêu thụ, nên 160 kg oxy mỗi ngày nghĩa là
hơn 200 kg CO2 cần loại bỏ. Với tỷ lệ khí trên nước 5:1 đến 10:1, lưu lượng 950 m³/h cần quạt thổi khoảng 4750 đến
9500 m³ không khí mỗi giờ.

## Tải lượng chất rắn và kích thước lọc trống
Tải lượng chất rắn của RAS vào khoảng 25 đến 30 % khối lượng thức ăn dưới dạng chất rắn lơ lửng khô, nên 400 kg
thức ăn tạo ra khoảng 100 đến 120 kg chất rắn mỗi ngày. Lọc trống phải có công suất định mức cho toàn bộ lưu lượng
tuần hoàn ở mức tải chất rắn đó, thường thêm biên dự phòng 20 đến 30 %. Lọc trống quá nhỏ sẽ rửa ngược gần như liên
tục và để chất rắn mịn lọt sang bộ lọc sinh học.

## Dự phòng và thiết bị backup
Hệ RAS phải được thiết kế sao cho không có hỏng hóc đơn lẻ nào của thiết bị có thể làm chết cá. Cách làm phổ biến
là bố trí bơm và máy thổi khí theo kiểu N+1, một máy phát điện dự phòng tự khởi động, và nguồn oxy khẩn cấp cấp vào
đĩa khuếch tán trong mọi bể khi hệ thống chính dừng. Vì cá có thể chết trong vòng một giờ khi mất dòng chảy hoặc
oxy, thiết bị dự phòng phải được kiểm tra thường xuyên chứ không chỉ lắp đặt.

## Biên thiết kế và tổng thể tích nước
Tổng thể tích nước của RAS lớn hơn thể tích bể, vì bể chứa (sump), các bộ lọc, bộ lọc sinh học và đường ống thường
cộng thêm 20 đến 40 %. Người thiết kế cũng thêm biên an toàn khoảng 20 đến 30 % vào các tải đỉnh về oxy, lưu lượng
và lọc sinh học, đồng thời chừa chỗ cho các bể trong tương lai. Một hệ RAS được tính vừa khít với mức đỉnh tính
toán sẽ không còn dư địa cho thời tiết nóng, cá lớn nhanh hơn hay thiết bị hao mòn.
