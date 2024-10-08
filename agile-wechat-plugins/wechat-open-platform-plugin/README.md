## 包路径导航
- **小程序**
  - [credential](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fcredential) 接口调用凭证
    - [CredentialApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fcredential%2FCredentialApi.java) 接口调用凭证
      - 获取接口调用凭据 `getAccessToken`
      - 获取稳定接口调用凭据 `getStableAccessToken`
  - [login](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Flogin) 小程序登录
    - [LoginApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Flogin%2FLoginApi.java) 小程序登录
      - 小程序登录 `code2Session`
      - 检验登录态 `checkSessionKey`
      - 重置登录态 `resetUserSessionKey`
  - [mini_code](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_code) 小程序码与小程序链接
    - [MiniCodeApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_code%2FMiniCodeApi.java) 小程序码
      - 获取小程序码 `getQRCode`
      - 获取不限制的小程序码 `getUnlimitedQRCode`
      - 获取小程序二维码 `createQRCode`
    - [UrlSchemaApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_code%2FUrlSchemaApi.java) URL Scheme
      - 获取 NFC 的小程序 scheme `generateNFCScheme`
      - 获取加密 scheme 码 `generateScheme`
      - 查询 scheme 码 `queryScheme`
    - [UrlLinkApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_code%2FUrlLinkApi.java) URL Link
      - 获取加密 URLLink `generateUrlLink`
      - 查询加密 URLLink `queryUrlLink`
    - [ShortLinkApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_code%2FShortLinkApi.java) Short Link
      - 获取 ShortLink `generateShortLink`
  - [mini_customer](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_customer) 小程序客服
    - [MiniCustomerMessageApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmini_customer%2FMiniCustomerMessageApi.java) 客服消息
      - 获取客服消息内的临时素材 `getTempMedia`
      - 下发客服当前输入状态 `setTyping`
      - 新增图片素材 `uploadTempMedia`
      - 发送客服消息 `sendCustomMessage`
  - [wechat_customer](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fwechat_customer) 微信客服
    - [WechatCustomerApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fwechat_customer%2FWechatCustomerApi.java) 微信客服
  - [message](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmessage) 消息相关
    - [DynamicMessageApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmessage%2FDynamicMessageApi.java) 动态消息
      - 创建 activity_id `createActivityId`
      - 修改动态消息 `setUpdatableMsg`
    - [SubscribeMessageApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fmessage%2FSubscribeMessageApi.java) 订阅消息
      - 删除模板 `deleteMessageTemplate`
      - 获取类目 `getCategory`
      - 获取关键词列表 `getPubTemplateKeyWordsById`
      - 获取所属类目下的公共模板 `getPubTemplateTitleList`
      - 获取个人模板列表 `getMessageTemplateList`
      - 发送订阅消息 `sendMessage`
      - 添加模板 `addMessageTemplate`
      - 激活与更新服务卡片 `setUserNotify`
      - 查询服务卡片状态 `getUserNotify`
      - 更新服务卡片扩展信息 `setUserNotifyExt`
  - [user](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fuser) 用户信息
    - [MobileApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fuser%2FMobileApi.java) 手机号
      - 获取手机号 `getPhoneNumber`
    - [NetworkApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fuser%2FNetworkApi.java) 网络
      - 获取用户 encryptKey `getUserEncryptKey`
    - [UserInfoApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fuser%2FUserInfoApi.java) 用户信息
      - 获取插件用户 openPId `getPluginOpenPId`
      - 检查加密信息 `checkEncryptedData`
      - 支付后获取 UnionID `getPaidUnionId`
  - [hardware](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fhardware) 硬件设备
    - [HardwareApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fmini%2Fhardware%2FHardwareApi.java) 硬件设备
      - 发送设备消息 `sendHardwareDeviceMessage`
      - 获取设备票据 `getSnTicket`
      - 创建设备组 `createIotGroupId`
      - 设备组删除设备 `removeIotGroupDevice`
      - 设备组添加设备 `addIotGroupDevice`
      - 查询设备组信息 `getIotGroupInfo`
      - 激活设备 license `activeLicenseDevice`
      - 查询 license 资源包列表 `getLicensePkgList`
- **微信开放平台**
  - [platform_credential](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/platform_credential) 第三方平台调用凭证
    - [CredentialApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fthird%2Fplatform_credential%2FCredentialApi.java) 凭证管理
      - 启动票据推送服务 `startPushTicket`
      - 获取预授权码 `getPreAuthCode`
      - 获取授权账号调用令牌 `getAuthorizerAccessToken`
      - 获取刷新令牌 `getAuthorizerRefreshToken`
      - 获取令牌 `getComponentAccessToken`
  - [auth_account_manage](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/auth_account_manage) 授权账号管理
  - [open_api](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/open_api) openApi管理
  - [platform_manage](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/platform_manage) 第三方平台管理
    - [DomainManageApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fthird%2Fplatform_manage%2Fdomain%2FDomainManageApi.java) 域名管理
      - 设置第三方平台服务器域名 `modifyThirdPartyServerDomain`
      - 设置第三方平台业务域名 `modifyThirdPartyJumpDomain`
      - 获取第三方平台业务域名校验文件 `getThirdPartyJumpDomainConfirmFile`
    - [TemplateManageApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fthird%2Fplatform_manage%2Ftemplate%2FTemplateManageApi.java) 模板库管理
      - 获取草稿箱列表 `getTemplatedRaftList`
      - 将草稿添加到模板库 `addToTemplate`
      - 获取模板列表 `getTemplateList`
      - 删除代码模板 `deleteTemplate`
  - [open_account_manage](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/open_account_manage) 开放平台账号管理
  - [min_register](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/min_register) 代商家注册小程序
  - [min_manage](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/min_manage) 代商家管理小程序
    - [MiniCodeApi.java](src%2Fmain%2Fjava%2Fio%2Fgithub%2Fthebesteric%2Fframework%2Fagile%2Fwechat%2Fthird%2Fplatform%2Fapi%2Fthird%2Fmin_manage%2Fcode%2FMiniCodeApi.java) 小程序代码管理
      - 获取隐私接口检测结果 `getCodePrivacyInfo`
      - 上传代码并生成体验版 `commit`
      - 获取已上传的代码页面列表 `getCodePage`
      - 获取体验版二维码 `getTrialQRCode`
      - 提交代码审核 `submitAudit`
      - 撤回代码审核 `undoAudit`
      - 发布已通过审核的小程序 `release`
      - 小程序版本回退 `revertCodeRelease`
      - 分阶段发布 `grayRelease`
      - 获取分阶段发布详情 `getGrayReleasePlan`
      - 取消分阶段发布 `revertGrayRelease`
      - 设置小程序服务状态 `setVisitStatus`
      - 查询各版本用户占比 `getSupportVersion`
  - [flow_operate](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/flow_operate) 小程序流量主代运营
  - [official_manage](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/official_manage) 代商家管理公众号
  - [cloud_batch](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/cloud_batch) 批量代云开发
  - [cloud_normal](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/cloud_normal) 普通代云开发
  - [wechat_cloud](src/main/java/io/github/thebesteric/framework/agile/wechat/third/platform/api/third/wechat_cloud) 微信云托管