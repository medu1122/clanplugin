chỉ t làm plugin này đi 
Ok 👍 giờ tao sẽ đứng vai trò architect + dev plugin cho Paper 1.21.4 cho mày.
Tao sẽ phân tích chuẩn theo kiểu production chứ không phải plugin rác.

I. Kiến trúc tổng thể
Server: Paper 1.21.4
Checking version, please wait... [15:40:59 INFO]: This server is running Paper version 1.21.4-232-ver/1.21.4@12d8fe0 (2025-06-09T10:15:42Z) (Implementing API version 1.21.4-R0.1-SNAPSHOT) You are running the latest versionAPI: Paper APIBuild tool: Maven hoặc Gradle (khuyên Maven)
Plugin chia thành 4 module logic:
- ClanManager
- TeamManager
- WarManager
- ESPManager
Vì clan chỉ là hiển thị + tổ chức, không can thiệp PvP .
Team mới là thứ chặn PvP.

II. Thiết kế Data Model
Clan
class Clan {
    String name;
    UUID owner;
    Set<UUID> members;
    Set<UUID> invitedPlayers;
    Set<UUID> joinRequests;
    int maxSlots = 50;
    int level;
    String bannerData; // optional
}
Team
class Team {
    UUID leader;
    Set<UUID> members;
    int maxSize = 5;
}

III. Danh sách command cần implement
🟣 Clan Commands
/clan top
/clan create <name>
/clan join <name>
/clan accept
/clan deny
/clan invite <player>
/clan kick <player>
/clan raccept <player>
/clan rdeny <player>
/clan requests
/clan upgrade
/clan war <clan>
/clan remove
/clan chat <message>
/clan getbanner
🔵 Team Commands
/team create
/team invite <player>
/team accept
/team leave
/team kick <player>
/team disband

IV. ESP Implementation (Quan trọng)
1️⃣ ESP màu bạc cho cùng clan
Cách làm:
•	Dùng Scoreboard Team API
•	Set glow effect
•	Set team color
Team team = scoreboard.registerNewTeam("clan_x");
team.setColor(NamedTextColor.GRAY);
team.addEntry(player.getName());
player.setGlowing(true);
Màu bạc → GRAY

2️⃣ ESP đỏ khi clan war
Khi /clan war <clan>:
•	Lưu vào WarManager
•	Khi player thuộc clan đối địch:
•	set glowing
•	set team color RED
team.setColor(NamedTextColor.RED);
Chỉ áp dụng khi war active.

V. PvP Logic
🔵 Clan
Không can thiệp PvP.
🔵 Team
Block PvP nếu:
@EventHandler
public void onDamage(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player damager)) return;
    if (!(e.getEntity() instanceof Player victim)) return;

    if (teamManager.sameTeam(damager, victim)) {
        e.setCancelled(true);
    }
}

VI. Disconnect Rule (Team)
Khi player disconnect:
@EventHandler
public void onQuit(PlayerQuitEvent e) {
    teamManager.removeFromTeam(e.getPlayer());
}
=> auto kick khỏi team.
Clan thì không.

VII. Rank / Title hiển thị
Khuyên dùng:
•	LuckPerms APIhoặc
•	Prefix qua scoreboard
Ví dụ:
[ClanName] PlayerName

VIII. Storage
⚠️ Không dùng HashMap tạm thời.
Chọn 1 trong 2:
🔹 SQLite (đề xuất)
Phù hợp server nhỏ
🔹 MySQL
Nếu server lớn

IX. Clan Upgrade System
Mỗi level:
Level 1 → 10 members
Level 2 → 20 members
...
Level 5 → 50 members
Upgrade tốn điểm cống hiến . đây là điểm chung của clan có thể kiếm được bằng cách member sell các item kiếm được từ việc đánh boss , khoáng sản và farm,fishing , mỗi level sẽ có buff đặc biệt như %speed , health , jump 

X. Kiến trúc Package
me.yourname.clans
 ├── ClanPlugin.java
 ├── clan/
 ├── team/
 ├── war/
 ├── storage/
 ├── listeners/
 └── commands/

XI. Những thứ khó nhất trong plugin này
1.	ESP đúng màu cho từng player khác nhau
2.	Xử lý war đúng khi 2 clan online
3.	Không gây conflict với scoreboard của plugin khác
4.	Sync data khi reload

XII. Nâng cấp sau này (optional)
•	GUI menu
•	Clan base
•	Clan bank
•	Clan power
•	Clan territory

XIII. Đánh giá độ khó
Phần	Độ khó
Clan cơ bản	⭐⭐
Team PvP block	⭐⭐
ESP System	⭐⭐⭐
War system	⭐⭐⭐⭐
Upgrade + economy	⭐⭐⭐
=> Tổng thể: Medium – Hard

lưu ram đối với team và lưu database đối với clan 

