# Cơ bản về RAS — Ghi dữ liệu và bảng theo dõi

## RAS nên ghi lại những dữ liệu gì
RAS nên ghi lại mọi số đo của cảm biến trực tuyến cùng với các ghi chép hằng ngày của người vận hành như lượng thức
ăn, số cá chết, lượng nước thay, hóa chất đã châm và công việc bảo dưỡng. Giá trị cảm biến thường được lưu mỗi 1–5
phút, còn ghi chép thủ công được nhập một hoặc vài lần mỗi ngày. Giữ cả hai trong cùng một hệ thống giúp liên hệ
chất lượng nước với kết quả nuôi cá.

## Chu kỳ ghi và lưu trữ dữ liệu
Chu kỳ ghi một phút là phổ biến với oxy hòa tan và nhiệt độ, còn các thông số thay đổi chậm như độ mặn có thể được
lưu mỗi 5–15 phút. Một cảm biến ghi mỗi phút tạo ra khoảng 525.000 giá trị mỗi năm, là một lượng nhỏ đối với cơ sở
dữ liệu hiện đại. Các trại thường giữ dữ liệu gốc ít nhất một năm và dữ liệu tổng hợp trong suốt vòng đời của trại.

## Bộ điều khiển, PLC và bộ ghi dữ liệu
Tín hiệu cảm biến trong RAS thường được thu thập bởi PLC, bộ điều khiển chuyên dụng hoặc bộ ghi dữ liệu, thường qua
tín hiệu 4–20 mA, Modbus hoặc các giao thức công nghiệp tương tự. Bộ điều khiển chuyển các giá trị đến hệ thống
SCADA hoặc cơ sở dữ liệu trên máy chủ tại chỗ hoặc trên đám mây. Một bản sao dữ liệu tại chỗ giúp trại vẫn hoạt
động khi mất kết nối internet.

## Dấu thời gian và độ chính xác của đồng hồ
Mỗi giá trị được ghi cần một dấu thời gian chính xác, vì người vận hành so sánh sự kiện giữa nhiều cảm biến và
nhiều bể. Mọi bộ điều khiển và máy chủ nên tự động đồng bộ đồng hồ, ví dụ với một máy chủ thời gian mạng, và lưu
thời gian theo một múi giờ duy nhất như UTC. Đồng hồ sai khiến không thể biết bơm hỏng trước hay sau khi oxy giảm.

## Bảng theo dõi RAS hiển thị gì
Bảng theo dõi (dashboard) RAS là màn hình cho thấy ngay tình trạng hiện tại của trại, thường với một ô cho mỗi bể
hiển thị oxy, nhiệt độ, pH và lưu lượng. Giá trị trong giới hạn thường hiện màu xanh lá, mức cảnh báo màu vàng và
mức nguy cấp màu đỏ. Một bảng theo dõi tốt cho người vận hành biết trong vài giây liệu có bể nào cần chú ý không.

## Biểu đồ xu hướng
Biểu đồ xu hướng vẽ giá trị cảm biến theo thời gian, như 24 giờ, 7 ngày hoặc 30 ngày gần nhất, và là công cụ chính
để phát hiện các thay đổi chậm trong RAS. Vẽ oxy, việc cho ăn và nhiệt độ trên cùng một biểu đồ cho thấy cá phản
ứng thế nào sau mỗi bữa ăn. Các ngưỡng cảnh báo vẽ trên biểu đồ giúp dễ thấy giá trị đã tiến gần mức nguy hiểm đến
đâu.

## Ghi chép thủ công và biểu mẫu số
Các số đo thủ công như kết quả máy quang kế cho TAN, nitrit và độ kiềm nên được nhập vào cùng hệ thống với dữ liệu
cảm biến, không chỉ lưu trên giấy. Biểu mẫu số trên máy tính bảng hoặc điện thoại có thể kiểm tra giá trị có nằm
trong khoảng hợp lý không trước khi lưu. Mỗi bản ghi nên cho biết ai đã nhập và nhập lúc nào.

## Chỉ số hiệu quả từ dữ liệu đã ghi
Dữ liệu RAS đã ghi có thể chuyển thành các chỉ số hiệu quả chính như hệ số chuyển đổi thức ăn, tốc độ tăng trưởng
đặc trưng, tỷ lệ chết, lượng nước dùng cho mỗi kilôgam cá và năng lượng dùng cho mỗi kilôgam cá. Các chỉ số này
thường được báo cáo theo từng bể và từng lứa mỗi tuần hoặc mỗi tháng. So sánh chỉ số giữa các lứa cho thấy những
thay đổi ở trại có thực sự hiệu quả hay không.

## Truy cập dữ liệu RAS từ xa
Truy cập từ xa cho phép quản lý và nhân viên trực xem bảng theo dõi RAS từ điện thoại hoặc máy tính bên ngoài trại.
Truy cập từ xa phải được bảo vệ bằng mật khẩu mạnh, tài khoản người dùng riêng và kết nối mã hóa, vì kẻ tấn công
thay đổi được điểm đặt có thể làm chết cá. Nhiều trại cho mọi nhân viên xem từ xa nhưng chỉ cho một số ít người đã
được đào tạo điều khiển từ xa.

## Chất lượng dữ liệu và khoảng trống cảm biến
Dữ liệu đã ghi chỉ hữu ích khi đúng, nên hệ thống dữ liệu RAS cần đánh dấu giá trị bị thiếu, số đo bị đứng yên và
giá trị nằm ngoài dải đo vật lý của cảm biến. Các lần hiệu chuẩn và vệ sinh cũng nên được ghi lại để giải thích
được những bước nhảy đột ngột trên biểu đồ xu hướng. Dữ liệu sai không được đánh dấu có thể dẫn đến quyết định sai
và kết luận sai.
