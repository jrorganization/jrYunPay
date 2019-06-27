package com.qh.common.config;

/**
 * @ClassName ConfigKeyConstant
 * @Description 系统级别的配置常量key
 * @Date 2017年10月30日 下午4:33:16
 * @version 1.0.0
 */
public class CfgKeyConst {
    /***极验验证Key******/
    public static final String geetest_key = "geetest_key";
    /***极验验证ID******/
    public static final String geetest_id = "geetest_id";
    /***报表文件路径******/
    public static final String  reportPath = "reportPath";
	/***系统配置参数 ip域名******/
    public static final String ip = "ip";
    /***系统配置文件路径***/
    public static final String payFilePath = "payFilePath";
    /***通道分类**/
    public static final String pay_channel_type = "pay_channel_type";
    /***平台公钥***/
    public static final String qhPublicKey = "qhPublicKey";
    /***平台私钥***/
//    public static final String qhPrivateKey = "qhPrivateKey";
    public static final String qhPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJEBMN2VYiN5ceWpWmfYKLAxa2RMlE733xQ+VigbD1xLtklVTy0KFUqKdRunYcIx2kYex2cDDjeLHDj27Qr6ZALKjb9jodRE3ByW84ha3ysGgfcWaOWueSYLyXIPxQu28ZQrbfo7SM/jfS3N0JtpClFQ+o8QfmlYrhNBTHelx+Q9AgMBAAECgYBQw08KO6e7hmrPtbGq4RRYfk4IQTbhfOF9DvNBx0CY8XqIAcHlnhYZvGBZlLK4TLf8EiyRzXvfp9WCTNXeJXQJ2dQV1pHRunq9jIn/KZJRb52jAkQ0qx82SPEsYPJCbjp+dZsT06JCvbcd3iTZi22g1RQRHOwz4iSAq1hvhjCr6QJBANjS8/maKXQMfCMcNJDXyM+hawzNuz3JgCtHJZE3cf/jeoWqgmLswiJ51/TmGAJbCSsEvRxDi4IGMo6DmfEayBcCQQCrNEgUCPvRPhms+YpT5nTCDkwpMxh9pccBveqvklwe5tlOgdPXQDjmaLVuEfjtpARBIzV+ey/guWQryNwk+NbLAkEAj/giv825ULEpjDaiQLrHP/aymiHQ/knZrOLk8vOZ4ostQ6vgP8dtcG7vElHmB0pjYAkZeLbw3zk2QKLpiMp7qQJATqjrwQDLqjytEVNp4diNpqdpCLjoNLqZL8yxak+FsdEA4Ng3m7tvKTXMvjDVvWHRbpgduOoiek7TnmZf90C5dwJBAKU0f1QcHh6pIRCXFGEzW4sMzgJhQi+6zSa1ggEoMnN/19eAyVJG12VISbOvdTU8TtwsrSIikNQ3/dkh2nH80UA=";
    /***发送邮箱信息***/
    public static final String email_message = "email_message";
    /***客户未付款订单超时时间***/
    public static final String cust_not_pay_expired_time = "cust_not_pay_expired_time";
    /***承兑商超时充值自动确认***/
    public static final String accp_confirm_expired_time = "accp_confirm_expired_time";
    /***承兑商超时提现超时未确认***/
    public static final String accp_acp_confirm_expired_time = "accp_acp_confirm_expired_time";

    /***平台私钥路径****/
    public static final String privateKeyPath = "privateKeyPath";
    /***平台商户默认密码***/
    public static final String pass_default_merch = "pass_default_merch";
    /***平台商户默认状态***/
    public static final String state_default_merch = "state_default_merch";
    /***平台代理默认密码***/
    public static final String pass_default_agent = "pass_default_agent";
    /***平台代理默认状态***/
    public static final String state_default_agent = "state_default_agent";
    /***平台支付域名****/
    public static final String pay_domain = "pay_domain";
    /***平台前台回调设置***/
    public static final String pay_return_url = "pay_return_url";
    /***平台后台通知设置***/
    public static final String pay_notify_url = "pay_notify_url";
    /***平台支付跳转中间页面***/
    public static final String pay_jump_url = "pay_jump_url";
    /***平台支付跳转绑卡页面***/
    public static final String pay_card_url = "pay_card_url";
    /***平台扫码通道跳转扫码页面**********/
    public static final String pay_qr_url = "pay_qr_url";
    /***平台代付前台回调设置***/
    public static final String pay_acp_return_url = "pay_acp_return_url";
    /***平台代付后台通知设置***/
    public static final String pay_acp_notify_url = "pay_acp_notify_url";

    /***使用哪个短信发送平台***/
    public static final String sms_send_type = "sms_send_type";
    /***使用哪个短信发送平台***/
    public static final String sms_send_type_f = "sms_send_type_f";
    /***收银台路径PC***/
    public static final String cashier_url_pc = "/pay/cashier/one";

    /***收银台路径移动端***/
    public static final String cashier_url_mobile = "/pay/cashier/one_m";

    /****************************************************短信配置********************************************/

    /***阿里云注册模板编号***/
    public static final String sms_aliy_tmpl_code_reg = "sms_aliy_tmpl_code_reg";
    /***阿里云注册模板内容  未配置sms_aliy_tmpl_code_reg时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_reg = "sms_aliy_tmpl_ctx_default_reg";
    /***阿里云确认模板编号***/
    public static final String sms_aliy_tmpl_code_confirm = "sms_aliy_tmpl_code_confirm";
    /***阿里云确认模板默认内容  未配置sms_aliy_tmpl_code_confirm时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_confirm = "sms_aliy_tmpl_ctx_default_confirm";
    /***阿里云通知模板编号***/
    public static final String sms_aliy_tmpl_code_notify = "sms_aliy_tmpl_code_notify";
    /***阿里云确认模板默认内容  未配置sms_aliy_tmpl_code_notify时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_notify = "sms_aliy_tmpl_ctx_default_notify";

    /***阿里云短信账号***/
    public static final String sms_aliy_accesskey_id = "sms_aliy_accesskey_id";
    /***阿里云短信密码***/
    public static final String sms_aliy_accesskey_secret = "sms_aliy_accesskey_secret";
    /***阿里云短信签名***/
    public static final String sms_aliy_sign_name = "sms_aliy_sign_name";

    /***亚马逊短信账号***/
    public static final String sms_aws_access_key = "sms_aws_access_key";
    /***亚马逊短信密码***/
    public static final String sms_aws_secret_key = "sms_aws_secret_key";

    /**云信互联账号**/
    public static final String sms_yun_access_key = "sms_yun_access_key";
    /**云信互联密码**/
    public static final String sms_yun_secret_key = "sms_yun_secret_key";
    /**云信互联接口地址**/
    public static final String sms_yun_host_url = "sms_yun_host_url";
    /**云信互联接入码**/
    public static final String sms_yun_extno_key= "sms_yun_extno_key";


    /***发件人邮箱（用来发送邮件的邮箱地址）***/
    public static final String email_account = "email_account";
    /***发件人邮箱smtp授权码***/
    public static final String email_password = "email_password";
    /***发件人邮箱的 SMTP 服务器地址***/
    public static final String email_smtp_host = "email_smtp_host";

    /**短信通道**/
   public static final String send_message_yorn = "send_message_yorn";
    /**承兑商接单短信通知开关******/
    public static final String accp_order_notify_sms = "accp_order_notify_sms";

    /***腾讯云注册模板编号***/
    public static final String sms_tx_tmpl_code_reg = "sms_tx_template_code_reg";
    /***腾讯云注册模板默认内容  未配置sms_tx_tmpl_code_reg时生效 ***/
    public static final String sms_tx_tmpl_ctx_default_reg = "sms_tx_tmpl_ctx_default_reg";
    /***腾讯云确认模板编号***/
    public static final String sms_tx_tmpl_code_confirm = "sms_tx_tmpl_code_confirm";
    /***腾讯云确认模板默认内容  未配置sms_tx_tmpl_code_confirm时生效 ***/
    public static final String sms_tx_tmpl_ctx_default_confirm = "sms_tx_tmpl_ctx_default_confirm";
    /***腾讯云通知模板编号***/
    public static final String sms_tx_tmpl_code_notify = "sms_tx_tmpl_code_notify";
    /***腾讯云通知模板默认内容  未配置sms_tx_tmpl_code_notify时生效***/
    public static final String sms_tx_tmpl_ctx_default_notify = "sms_tx_tmpl_ctx_default_notify";

    /***腾讯云短信appid***/
    public static final String sms_tx_appid = "sms_tx_appid";
    /***腾讯云短信appkey***/
    public static final String sms_tx_appkey = "sms_tx_appkey";
    /***腾讯云短信签名***/
    public static final String sms_tx_sign_name = "sms_tx_sign_name";
    
    public static final String sms_send_type_tx = "tx";
    public static final String sms_send_type_aliy = "aliy";
    
    /**修改密码验证码key**/
    public static final String sms_code_phone_update_pass = "sms_code_phone_update_pass_";

    /******************************************************************************************************/
    
    /**支付通道单次轮洵比例**/
    public static final String COMPANY_SIGLE_POLL_MONEY = "company_single_poll_money";

    /**下发自动审核配置**/
    public static final String PAY_AUDIT_AUTO_ACP = "pay_audit_auto_acp";
    
    /**商户单日 和单月限额key**/
    public static final String MERCHANT_DAY_LIMIT = "merchant_day_limit_";
    public static final String MERCHANT_MONTH_LIMIT = "merchant_month_limit_";
    
    /**是否关闭支付**/
    public static final String COMPANY_CLOSE_PAY = "company_close_pay";
    /**是否关闭下发**/
    public static final String COMPANY_CLOSE_ACP = "company_close_acp";
    /**是否关闭提现**/
    public static final String COMPANY_CLOSE_WITHDRAW = "company_close_withdraw";
    /**商户给承兑商利润差 用于充值部分（百分比)*****/
    public static final String accp_merch_dis_profit = "accp_merch_dis_profit";

    /**商户给承兑商利润差 用于提现部分（百分比)*****/
    public static final String accp_merch_acp_dis_profit = "accp_merch_acp_dis_profit";
    /**提现手续费******/
    public static final String withdraw_hand_fee = "withdraw_hand_fee";



    /***今日订单平均确认时间(权重)*******/
    public static final String weight_today_confirm_time = "weight_today_confirm_time";
    /***今日申诉订单数(权重)*****/
    public static final String weight_today_appeal_order_num = "weight_today_appeal_order_num";
    /***七日订单平均确认时间(权重)*******/
    public static final String weight_week_confirm_time = "weight_week_confirm_time";
    /***七日申诉百分比(权重)*****/
    public static final String weight_week_appeal_order_per = "weight_week_appeal_order_per";

    /****承兑商充值默认手续费(USDT)****/
    public static final String accp_charge_poundage_single = "accp_charge_poundage_single";
    /****承兑商提现默认手续费(USDT)****/
    public static final String accp_withdraw_poundage_single = "accp_withdraw_poundage_single";
    /****承兑商最小提现数量(USDT)****/
    public static final String accp_withdraw_min_limit = "accp_withdraw_min_limit";

    //提现设置 自动审核最小值
    public static final String withdraw_auto_audit_min = "withdraw_auto_audit_min";
    //提现设置 自动审核最大值
    public static final String withdraw_auto_audit_max = "withdraw_auto_audit_max";
    //提现设置 自动审核标志
    public static final String withdraw_auto_audit_flag = "withdraw_auto_audit_flag";

    //每个商户 userId 在途订单最多笔数
    public static final String max_merch_per_user_pay_num = "max_merch_per_user_pay_num";
    //承兑商中每个客户付款金额失效时间
    public static final String accp_cust_de_amount_minute = "accp_cust_de_amount_minute";
    public static String qr_money_path = "qr_money_path";
}
