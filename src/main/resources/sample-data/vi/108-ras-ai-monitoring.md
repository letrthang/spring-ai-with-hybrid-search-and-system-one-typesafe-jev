# Cơ bản về RAS — Giám sát bằng AI và phát hiện bất thường

## Giám sát bằng AI trong RAS là gì
Giám sát bằng AI trong RAS là phần mềm đọc dữ liệu cảm biến và dữ liệu vận hành của trại, rồi dùng học máy để phát
hiện các mẫu bất thường và dự báo sự cố. Giám sát bằng AI hoạt động bên trên hệ thống cảnh báo thông thường chứ
không thay thế hệ thống đó. Mục tiêu là cảnh báo người vận hành sớm hơn và kèm nhiều thông tin hơn so với các
ngưỡng cảnh báo cố định.

## Dữ liệu mà hệ thống giám sát AI sử dụng
Hệ thống giám sát AI cho RAS dùng các giá trị cảm biến đã ghi như oxy hòa tan, nhiệt độ, pH, ORP, lưu lượng và mực
nước, cùng với giờ cho ăn, lượng thức ăn, sinh khối và hồ sơ bảo dưỡng. Lịch sử dữ liệu càng nhất quán và đầy đủ
thì hệ thống học càng tốt. Hầu hết hệ thống cần vài tuần đến vài tháng dữ liệu sạch trước khi dự báo trở nên đáng
tin cậy.

## Học các mẫu bình thường
Hệ thống giám sát AI học điều gì là bình thường với từng bể, như oxy giảm hằng ngày sau mỗi lần cho ăn và CO2 tăng
chậm khi cá lớn lên. Thay vì một ngưỡng cố định, hệ thống xây dựng một khoảng kỳ vọng thay đổi theo giờ trong ngày,
việc cho ăn và sinh khối. Một giá trị bị coi là bất thường khi ra ngoài khoảng kỳ vọng đó, dù vẫn nằm trong ngưỡng
cảnh báo cố định.

## Phát hiện bất thường
Phát hiện bất thường là phần của hệ thống giám sát AI tìm ra những số đo hoặc tổ hợp số đo không khớp với mẫu bình
thường đã học. Ví dụ là oxy giảm nhanh hơn thường lệ sau bữa ăn, cảm biến lưu lượng giữ nguyên đúng một giá trị
suốt nhiều giờ, hoặc một bể hoạt động khác với các bể giống hệt nó. Nhiều bất thường là dấu hiệu sớm của hỏng thiết
bị, cảm biến bám bẩn hoặc cá bị stress.

## Dự báo sự cố oxy
Hệ thống giám sát AI có thể dự báo tình trạng thiếu oxy bằng cách kéo dài xu hướng oxy gần đây về phía trước và so
với diễn biến thường thấy sau khi cho ăn. Nếu oxy có khả năng giảm dưới mức cảnh báo trong, ví dụ, 15–30 phút tới,
hệ thống có thể báo cho người vận hành trước khi chạm ngưỡng cảnh báo. Cảnh báo sớm cho nhân viên thời gian kiểm
tra nguồn oxy, đĩa khuếch tán và bơm.

## Dự báo amoniac và nitrit tăng
Hệ thống giám sát AI có thể ước tính nguy cơ TAN hoặc nitrit tăng bằng cách kết hợp tải lượng thức ăn, nhiệt độ
nước, pH, lưu lượng qua bộ lọc sinh học và kết quả đo thủ công gần đây. Tăng lượng cho ăn đột ngột, nhiệt độ giảm
hoặc lưu lượng bộ lọc sinh học có vấn đề đều có thể làm tăng nguy cơ dự báo. Vì cảm biến amoniac trực tuyến kém tin
cậy hơn, các dự báo này nên được xác nhận bằng phép đo máy quang kế.

## Giải thích cảnh báo
Một cảnh báo AI hữu ích phải giải thích vì sao nó được phát ra, ví dụ nêu tên các cảm biến liên quan, hiển thị
khoảng kỳ vọng bên cạnh giá trị thực tế và chỉ ra các sự kiện gần đây như một lần cho ăn hoặc một lần đổi bơm. Giải
thích rõ ràng giúp người vận hành tin vào cảnh báo và nhanh chóng quyết định cần kiểm tra gì. Một cảnh báo không
giải thích được thì khó xử lý và thường bị bỏ qua.

## Giữ con người trong vòng quyết định
Trong RAS, giám sát bằng AI nên tư vấn cho con người thay vì tự điều khiển các thiết bị quan trọng. Người vận hành
xác nhận cảnh báo, quyết định hành động và ghi lại những gì đã phát hiện, còn các chức năng an toàn như oxy dự
phòng và cảnh báo cố định vẫn độc lập với AI. Giữ con người trong vòng quyết định giúp một dự báo sai không gây hại
cho cá.

## Học từ phản hồi của người vận hành
Phản hồi của người vận hành giúp hệ thống giám sát AI tốt hơn theo thời gian, vì mỗi cảnh báo có thể được đánh dấu
là sự cố thật, lỗi cảm biến hay cảnh báo giả. Hệ thống dùng các nhãn này để điều chỉnh độ nhạy và học các mẫu mới,
như thay đổi bình thường sau khi phân cỡ cá. Phản hồi đều đặn giúp giảm cảnh báo giả và tránh tình trạng nhờn cảnh
báo.

## Giới hạn của giám sát bằng AI
Giám sát bằng AI chỉ tốt bằng dữ liệu mà nó học, nên cảm biến bị bẩn hoặc chưa hiệu chuẩn sẽ dẫn đến dự báo sai. Hệ
thống giám sát AI cũng có thể bỏ sót những sự kiện chưa từng gặp, như một bệnh mới hoặc một hỏng hóc thiết bị hiếm
gặp. Chăm sóc cảm biến tốt, cảnh báo nối dây cứng độc lập và nhân viên giàu kinh nghiệm vẫn là thiết yếu trong mọi
RAS.
