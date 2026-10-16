# Cơ bản về RAS — Cảnh báo và tự động hóa

## Mức cảnh báo và mức nguy cấp
Cảnh báo trong RAS thường có hai mức: mức cảnh báo khi một giá trị bắt đầu lệch, và mức nguy cấp khi cá gặp nguy
hiểm ngay lập tức. Ví dụ, bể cá hồi có thể cảnh báo khi DO dưới 7 mg/L và chuyển sang nguy cấp khi dưới 5 mg/L. Mức
cảnh báo cho nhân viên thời gian xử lý trước khi xảy ra khủng hoảng.

## Độ trễ cảnh báo và vùng trễ
Độ trễ cảnh báo buộc một giá trị phải nằm ngoài ngưỡng trong một khoảng thời gian nhất định, thường từ 30 giây đến
vài phút, rồi cảnh báo mới phát. Vùng trễ (hysteresis) nghĩa là cảnh báo chỉ tắt khi giá trị quay về hẳn bên trong
ngưỡng. Cả hai cùng ngăn các đỉnh ngắn và nhiễu cảm biến gây ra hàng loạt cảnh báo giả.

## Thông báo và chuyển cấp cảnh báo
Khi RAS phát cảnh báo, hệ thống giám sát báo cho nhân viên đang trực bằng cuộc gọi, tin nhắn hoặc ứng dụng. Nếu
không ai xác nhận cảnh báo nguy cấp trong một khoảng thời gian nhất định, ví dụ 5 đến 10 phút, cảnh báo được chuyển
cho người tiếp theo trong danh sách gọi. Luôn phải bố trí người trực đêm.

## Oxy dự phòng tự động
Oxy dự phòng tự động mở van cấp oxy cho các đĩa khuếch tán khẩn cấp ở mọi bể khi DO xuống dưới mức nguy cấp hoặc
nguồn oxy chính bị hỏng. Van này thường là loại thường mở, nên nó tự mở khi mất điện. Oxy dự phòng thường lấy từ bồn
oxy lỏng hoặc bình áp cao.

## Bơm dự phòng
Bơm dự phòng là bơm thứ hai tự động khởi động khi bơm đang chạy bị hỏng hoặc lưu lượng giảm dưới giá trị cài đặt. Các
bơm quan trọng, như bơm tuần hoàn chính, luôn phải có bơm dự phòng. Trại luân phiên bơm chạy và bơm dự phòng mỗi tuần
hoặc mỗi tháng để cả hai luôn hoạt động tốt.

## Xử lý khi mất điện
Mất điện là sự cố nguy hiểm nhất trong RAS, vì bơm, nguồn oxy và sục khí đều dừng cùng lúc. Máy phát điện dự phòng
phải tự khởi động trong vài giây và gánh các tải quan trọng. Cảnh báo chạy pin và oxy dự phòng phải hoạt động được
ngay cả khi máy phát không khởi động.

## Kiểm tra cảnh báo và hệ thống dự phòng
Cảnh báo và hệ thống dự phòng phải được kiểm tra theo lịch, thường mỗi tuần hoặc mỗi tháng, bằng cách giả lập sự cố.
Việc kiểm tra gồm chạy máy phát có tải, mở van oxy dự phòng và kích hoạt cảnh báo từ từng cảm biến. Hệ thống dự phòng
chưa từng được kiểm tra thì không thể tin cậy trong tình huống khẩn cấp thật.

## Cảnh báo cảm biến mất kết nối
Cảnh báo cảm biến mất kết nối phát khi cảm biến ngừng gửi số đo hoặc gửi giá trị vô lý, như con số đứng yên hoặc DO
bằng không. Nếu không có cảnh báo này, một cảm biến hỏng có thể che giấu vấn đề thật. Cảnh báo mất kết nối cần được
xử lý nghiêm túc như số đo thấp cho đến khi tìm ra nguyên nhân.

## Khóa liên động giữa các thiết bị
Khóa liên động là quy tắc tự động dừng một thiết bị khi thiết bị khác hỏng, để bảo vệ cá hoặc máy móc. Ví dụ, máy tạo
ozone phải dừng khi dòng nước qua bể tiếp xúc ngừng chảy, và máy cho ăn phải dừng khi DO thấp ở mức nguy cấp. Khóa
liên động ngăn một sự cố gây ra sự cố thứ hai.

## Tránh tình trạng nhờn cảnh báo
Nhờn cảnh báo xảy ra khi nhân viên nhận quá nhiều cảnh báo không quan trọng đến mức bắt đầu bỏ qua cả cảnh báo thật.
Trại tránh tình trạng này bằng cách đặt ngưỡng hợp lý, dùng độ trễ, sửa nhanh cảm biến lỗi và xem lại nhật ký cảnh
báo mỗi tuần. Mỗi cảnh báo phải đi kèm một hành động rõ ràng.
