# ClanCore - Plugin Quản Lý Clan và Team cho Minecraft

ClanCore là một plugin Minecraft Paper/Spigot mạnh mẽ giúp người chơi tạo và quản lý các clan và team trong server. Plugin hỗ trợ đầy đủ các tính năng từ quản lý thành viên, chiến tranh giữa các clan, hệ thống nâng cấp clan với buffs, đến các công cụ ESP và chat riêng.

## ✨ Tính Năng Chính

### 🏰 Hệ Thống Clan
- **Tạo và Quản Lý Clan**: Tạo clan, mời thành viên, quản lý quyền hạn
- **Hệ Thống Nâng Cấp**: Nâng cấp clan bằng clan points để nhận buffs và tăng số lượng thành viên
- **Chiến Tranh**: Khai chiến với các clan khác, hiển thị đối thủ bằng hiệu ứng đỏ
- **Chat Riêng**: Chat riêng trong clan với format đẹp mắt
- **ESP System**: Hiển thị thành viên cùng clan bằng hiệu ứng xám, đối thủ chiến tranh bằng đỏ
- **Lưu Trữ Database**: Tất cả dữ liệu clan được lưu vào SQLite, không mất khi restart server

### 👥 Hệ Thống Team
- **Tạo Team Nhanh**: Tạo team tạm thời để làm việc nhóm
- **Quản Lý Team**: Mời, kick, rời team dễ dàng
- **Chat Team**: Chat riêng trong team
- **Tự Động Disband**: Team tự động giải tán khi leader disconnect (hoặc promote member khác)
- **Lưu Trữ RAM**: Team chỉ lưu trong RAM, tự động xóa khi server restart

### ⚔️ Tính Năng PvP
- **Chặn PvP Team**: Người chơi cùng team không thể đánh nhau
- **Chiến Tranh Clan**: Hệ thống khai chiến giữa các clan

### 📊 Hệ Thống Nâng Cấp Clan
- **Clan Points**: Bán vật phẩm để lấy clan points
- **Nâng Cấp Level**: Sử dụng clan points để nâng cấp clan
- **Buffs Cộng Dồn**: Mỗi level cộng thêm buffs tốc độ và máu
- **Tăng Số Lượng Thành Viên**: Level cao hơn cho phép nhiều thành viên hơn

### 🎮 Giao Diện GUI
- **Clan Info GUI**: Xem danh sách thành viên với player heads, trạng thái online/offline, phân trang
- **Team Info GUI**: Xem thông tin team tương tự clan
- **Clan Upgrade GUI**: Xem thông tin nâng cấp, bán vật phẩm để lấy điểm
- **Sell Items GUI**: Giao diện bán vật phẩm với hiển thị giá trị và tổng điểm

## 📋 Danh Sách Lệnh

### 🏰 Lệnh Clan (`/clan`)

#### Quản Lý Clan Cơ Bản
- `/clan create <tên>` hoặc `/clan c <tên>` - Tạo clan mới
- `/clan join <tên>` hoặc `/clan j <tên>` - Gửi yêu cầu tham gia clan
- `/clan leave` hoặc `/clan l` - Rời khỏi clan
- `/clan info` hoặc `/clan in` - Xem thông tin clan (GUI)
- `/clan list` hoặc `/clan li` - Xem danh sách tất cả clans trong server

#### Mời và Yêu Cầu Tham Gia
- `/clan invite <người chơi>` hoặc `/clan i <người chơi>` - Mời người chơi vào clan (chỉ chủ clan)
- `/clan accept` hoặc `/clan a` - Chấp nhận lời mời tham gia clan
- `/clan deny` hoặc `/clan d` - Từ chối lời mời tham gia clan
- `/clan raccept <người chơi>` hoặc `/clan ra <người chơi>` - Chấp nhận yêu cầu tham gia (chỉ chủ clan)
- `/clan rdeny <người chơi>` hoặc `/clan rd <người chơi>` - Từ chối yêu cầu tham gia (chỉ chủ clan)
- `/clan requests` hoặc `/clan req` - Xem danh sách yêu cầu tham gia (chỉ chủ clan)

#### Quản Lý Thành Viên
- `/clan kick <người chơi>` hoặc `/clan k <người chơi>` - Đuổi thành viên khỏi clan (chỉ chủ clan)
- `/clan transfer <người chơi>` hoặc `/clan t <người chơi>` - Chuyển quyền sở hữu clan cho thành viên khác (chỉ chủ clan)

#### Chiến Tranh
- `/clan war <clan>` hoặc `/clan w <clan>` - Khai chiến với clan khác (chỉ chủ clan)

#### Nâng Cấp và Điểm
- `/clan upgrade` hoặc `/clan up` - Mở GUI nâng cấp clan (chỉ chủ clan)
  - Trong GUI có thể bán vật phẩm để lấy clan points
  - Sử dụng clan points để nâng cấp level

#### Chat
- `/clan chat <tin nhắn>` hoặc `/clan ch <tin nhắn>` - Gửi tin nhắn trong clan

#### Trợ Giúp
- `/clan` - Hiển thị trang đầu tiên của danh sách lệnh
- `/clan help <số trang>` hoặc `/clan h <số trang>` - Xem các trang khác của danh sách lệnh

### 👥 Lệnh Team (`/team`)

#### Quản Lý Team Cơ Bản
- `/team create` hoặc `/team c` - Tạo team mới
- `/team leave` hoặc `/team l` - Rời khỏi team
- `/team info` hoặc `/team in` - Xem thông tin team (GUI)
- `/team list` hoặc `/team li` - Xem danh sách tất cả teams đang hoạt động

#### Mời và Tham Gia
- `/team invite <người chơi>` hoặc `/team i <người chơi>` - Mời người chơi vào team (chỉ leader)
- `/team accept` hoặc `/team a` - Chấp nhận lời mời tham gia team

#### Quản Lý Thành Viên
- `/team kick <người chơi>` hoặc `/team k <người chơi>` - Đuổi thành viên khỏi team (chỉ leader)
- `/team disband` hoặc `/team d` - Giải tán team (chỉ leader)
- `/team transfer <người chơi>` hoặc `/team t <người chơi>` - Chuyển quyền sở hữu team cho thành viên khác (chỉ leader)

#### Chat
- `/team chat <tin nhắn>` hoặc `/team ch <tin nhắn>` - Gửi tin nhắn trong team

#### Trợ Giúp
- `/team` - Hiển thị trang đầu tiên của danh sách lệnh
- `/team help <số trang>` hoặc `/team h <số trang>` - Xem các trang khác của danh sách lệnh

### 🔧 Lệnh Admin

#### Clan Admin (`/clanadmin`) - Yêu cầu quyền `clancore.admin`
- `/clanadmin givepoints <clan> <số điểm>` hoặc `/clanadmin gp <clan> <số điểm>` - Cho điểm clan cho clan chỉ định
- `/clanadmin setlevel <clan> <level>` hoặc `/clanadmin sl <clan> <level>` - Set level cho clan (1-5)
- `/clanadmin tpall <clan>` hoặc `/clanadmin tp <clan>` - Teleport tất cả thành viên clan đến vị trí của bạn

#### Team Admin (`/teamadmin`) - Yêu cầu quyền `clancore.admin`
- `/teamadmin tpall <người chơi>` hoặc `/teamadmin tp <người chơi>` - Teleport tất cả thành viên team của người chơi đến vị trí của bạn

## 🎯 Tính Năng Chi Tiết

### Hệ Thống ESP (Glowing Effects)
- **Thành viên cùng clan**: Hiển thị với hiệu ứng màu xám
- **Đối thủ chiến tranh**: Hiển thị với hiệu ứng màu đỏ
- Tự động cập nhật khi join server hoặc khi có thay đổi về clan/war

### Hệ Thống Buffs
- **Tốc độ**: Tăng tốc độ di chuyển dựa trên level clan (cộng dồn)
- **Máu**: Tăng máu tối đa dựa trên level clan (cộng dồn)
- Buffs tự động áp dụng khi join server hoặc khi clan level up
- Buffs tự động gỡ khi rời clan, bị kick, hoặc quit server

### Hệ Thống Clan Points
- Bán vật phẩm trong GUI để lấy clan points
- Các vật phẩm có thể bán được cấu hình trong `config.yml`
- Clan points được lưu trong database, không mất khi restart

### Hệ Thống Level Clan
- **Level 1**: Tối đa 15 thành viên, +10% tốc độ
- **Level 2**: Tối đa 25 thành viên, +20% tốc độ, +15% máu
- **Level 3**: Tối đa 35 thành viên, +30% tốc độ, +30% máu
- **Level 4**: Tối đa 45 thành viên, +45% tốc độ, +45% máu
- **Level 5**: Tối đa 50 thành viên, +60% tốc độ, +60% máu

### Phân Quyền
- **Chủ Clan**: Có quyền mời, kick, accept/deny requests, khai chiến, nâng cấp, chuyển quyền
- **Thành Viên Clan**: Có thể rời clan, xem info, chat
- **Leader Team**: Có quyền mời, kick, disband, chuyển quyền
- **Thành Viên Team**: Có thể rời team, xem info, chat

### Lưu Trữ Dữ Liệu
- **Clan**: Lưu trong SQLite database (persistent)
  - Thông tin clan: tên, owner, level, contribution, clan_points
  - Danh sách thành viên với role
- **Team**: Lưu trong RAM (temporary)
  - Tự động xóa khi server restart
  - Tự động promote leader mới hoặc disband khi leader disconnect

## ⚙️ Cài Đặt

1. Tải file JAR từ releases
2. Đặt file vào thư mục `plugins` của server Paper/Spigot
3. Khởi động server để plugin tự động tạo file `config.yml`
4. Cấu hình các vật phẩm có thể bán và giá trị trong `config.yml`
5. Restart server hoặc reload plugin

## 📝 Cấu Hình

File `config.yml` cho phép bạn cấu hình:

- **Clan Points System**: 
  - Chi phí nâng cấp cho mỗi level
  - Danh sách vật phẩm có thể bán và giá trị điểm

- **Clan Level System**:
  - Số lượng thành viên tối đa cho mỗi level
  - Buffs tốc độ và máu cho mỗi level

Xem file `config.yml` mẫu để biết chi tiết cấu hình.

## 🔐 Permissions

- `clancore.admin` - Quyền sử dụng các lệnh admin (`/clanadmin`, `/teamadmin`)
- Mặc định: Tất cả người chơi có thể sử dụng `/clan` và `/team`

## 📌 Lưu Ý

- Clan data được lưu trong SQLite, không mất khi restart server
- Team data chỉ lưu trong RAM, sẽ mất khi restart server
- Khi leader team disconnect, team sẽ tự động promote member đầu tiên làm leader mới, hoặc disband nếu không còn member
- Khi player disconnect, họ sẽ tự động bị remove khỏi team nhưng vẫn giữ trong clan
- Buffs clan được áp dụng tự động và cộng dồn theo level
- ESP effects tự động cập nhật khi có thay đổi về clan membership hoặc war status

## 🎮 Hướng Dẫn Sử Dụng Nhanh

1. **Tạo Clan**: `/clan create TênClan`
2. **Mời Thành Viên**: `/clan invite <tên người chơi>`
3. **Xem Thông Tin**: `/clan info` (mở GUI)
4. **Nâng Cấp Clan**: `/clan upgrade` → Bán vật phẩm → Nâng cấp level
5. **Tạo Team**: `/team create`
6. **Mời Vào Team**: `/team invite <tên người chơi>`

## 📞 Hỗ Trợ

Nếu gặp vấn đề hoặc có câu hỏi, vui lòng tạo issue trên GitHub repository.

---

**ClanCore** - Plugin quản lý clan và team mạnh mẽ cho Minecraft Server của bạn!
