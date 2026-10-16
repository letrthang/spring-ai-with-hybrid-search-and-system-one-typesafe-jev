# Công nghệ sinh học cơ bản — tin sinh học

## Tin sinh học là gì
Tin sinh học là việc dùng máy tính và thống kê để lưu trữ, tìm kiếm và phân tích dữ liệu sinh học. Nó chủ yếu làm việc
với trình tự DNA, RNA và protein, nhưng cũng với hoạt động của gen và cấu trúc protein. Một lần chạy giải trình tự có
thể tạo ra hàng trăm gigabyte dữ liệu, quá nhiều để đọc bằng tay.

## Định dạng FASTA
FASTA là một định dạng văn bản đơn giản để lưu trình tự DNA hoặc protein. Mỗi bản ghi bắt đầu bằng một dòng tiêu đề
mở đầu bằng ">" theo sau là tên hoặc mô tả, và trình tự nằm ở các dòng tiếp theo. DNA dùng các chữ cái A, C, G và T,
còn protein dùng một chữ cái cho mỗi loại trong 20 amino acid.

## Định dạng FASTQ
FASTQ là định dạng văn bản mà máy giải trình tự dùng để lưu các lần đọc cùng với điểm chất lượng của chúng. Mỗi lần
đọc chiếm đúng bốn dòng: một dòng tiêu đề bắt đầu bằng "@", trình tự, một dòng "+" và một dòng ký tự chất lượng. Mỗi
ký tự mã hóa điểm chất lượng Phred của base nằm phía trên nó.

## Sắp gióng trình tự
Sắp gióng trình tự (alignment) xếp thẳng hàng hai hay nhiều trình tự để tìm chỗ chúng khớp nhau. Các khoảng trống được
chèn vào để tính đến những base được thêm vào hoặc bị mất, và các vị trí không khớp cho thấy sự thay thế. Sắp gióng
toàn cục so sánh các trình tự trên toàn bộ chiều dài, còn sắp gióng cục bộ tìm vùng khớp tốt nhất bên trong chúng.

## Tìm kiếm BLAST
BLAST là một chương trình được dùng rộng rãi để tìm trong cơ sở dữ liệu những trình tự giống với một trình tự truy
vấn. Nó hoạt động bằng cách tìm các đoạn khớp chính xác ngắn trước rồi mở rộng chúng. Mỗi kết quả có một giá trị E,
là số kết quả khớp tốt như vậy có thể xuất hiện do ngẫu nhiên, nên giá trị E gần bằng không nghĩa là kết quả có ý
nghĩa.

## Độ tương đồng và độ tương tự trình tự
Độ tương đồng trình tự (identity) là phần trăm số vị trí trong một bản sắp gióng mà hai trình tự có base hoặc amino
acid giống hệt nhau. Độ tương tự protein (similarity) còn tính cả những vị trí có amino acid khác nhau nhưng tính chất
hóa học giống nhau. Hai protein có độ tương đồng trên khoảng 30% theo chiều dài thường có cùng kiểu gấp nếp.

## Lắp ráp hệ gen
Lắp ráp hệ gen là ghép hàng triệu lần đọc ngắn trở lại thành trình tự hệ gen đầy đủ. Phần mềm tìm các lần đọc chồng
lên nhau và nối chúng thành những đoạn dài hơn gọi là contig. Các trình tự lặp lại làm việc lắp ráp trở nên khó, vì
các lần đọc từ những bản sao khác nhau của một đoạn lặp trông giống hệt nhau.

## Gióng lần đọc lên hệ gen tham chiếu
Gióng lần đọc (read mapping) là đặt mỗi lần đọc vào đúng vị trí khớp với nó trên một hệ gen tham chiếu đã biết. Cách
này nhanh hơn dựng một hệ gen mới từ đầu và là bước đầu tiên để tìm đột biến. Các lần đọc đã gióng được lưu ở định
dạng SAM hoặc định dạng nén BAM.

## Xác định biến thể
Xác định biến thể (variant calling) là tìm các vị trí mà DNA của mẫu khác với hệ gen tham chiếu. Phần mềm xem xét tất
cả các lần đọc được gióng vào mỗi vị trí và quyết định một khác biệt là thật hay chỉ là lỗi giải trình tự. Kết quả,
như các thay đổi một base gọi là SNP, được lưu trong tệp VCF.

## Cây phát sinh chủng loại
Cây phát sinh chủng loại là sơ đồ phân nhánh cho thấy mức độ họ hàng giữa các loài, các gen hoặc các chủng virus. Cây
được dựng bằng cách so sánh trình tự của chúng, với giả định rằng càng nhiều khác biệt thì thời gian từ tổ tiên chung
càng dài. Cây được dùng để lần theo quá trình tiến hóa của các loài và sự lây lan của các đợt dịch.
