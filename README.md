# WelcomeActions

Minecraft 高自由度的欢迎奖励插件。让玩家在聊天栏一键点击欢迎新人并获取奖励！

## 用法

安装插件后，编辑配置文件 `config.yml` 即可。

出于不同类型的服务器可能使用不同的登录方式考虑，本插件没有对特定登录插件做兼容。

如果你使用登录插件方式登录，请在登录服的登录插件设定
+ 玩家注册后，执行命令 `welcomeactions register %player_name%`
+ (AuthMe 可以在 `commands.yml` 中设置注册后执行命令)

如果你使用正版登录、外置登录等方式登录，请设定
+ 玩家初次进入服务器时，执行命令 `welcomeactions register %player_name%`
+ (CMI、Essentials 等基础插件都支持这个操作)

如果你想其它服区也能收到欢迎消息以及获取欢迎奖励，在该服区安装本插件，并且
+ 修改 `database.yml` 配置，使用 MySQL 数据库而非 SQLite 数据库
+ 必须要连接数据库，否则多服数据不会同步，其它服点击欢迎无法领取奖励

## 命令

根命令为 `/welcomeactions`，可缩写为 `/wactions` 或 `/wa`

+ `/wa reload` - 重载配置文件
+ `/wa reload database` - 重载`database.yml`并重新连接数据库
