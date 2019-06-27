/*
 Navicat MySQL Data Transfer

 Source Server         : 123456
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : localhost:3306
 Source Schema         : pay

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : 65001

 Date: 05/06/2019 10:23:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent
-- ----------------------------
DROP TABLE IF EXISTS `agent`;
CREATE TABLE `agent`  (
  `agent_id` int(11) NOT NULL,
  `agent_number` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理商户号',
  `status` int(1) NULL DEFAULT 0 COMMENT '状态  1 启用  0禁用',
  `audit_status` int(1) NULL DEFAULT 2 COMMENT '审核状态  1通过  0 待审核  2 不通过',
  `level` int(1) NULL DEFAULT 1 COMMENT '代理商级别    1 一级  2 二级',
  `parent_agent` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上级代理商户号  不填，默认一级代理(最大支持二级代理)',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `agent_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户名称   [代理商联系人信息开始]',
  `agent_short_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理商简称',
  `contacts` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contacts_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contacts_email` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `contacts_qq` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人QQ   [代理商联系人信息结束]',
  `coin_rate` json NULL COMMENT '币费率',
  `pay_channel_type` int(1) NULL DEFAULT NULL COMMENT '支付通道分类(走哪种分类的通道)',
  PRIMARY KEY (`agent_id`) USING BTREE,
  UNIQUE INDEX `unique_agent_agent_number`(`agent_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户代理(两级)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cfg_areas
-- ----------------------------
DROP TABLE IF EXISTS `cfg_areas`;
CREATE TABLE `cfg_areas`  (
  `id` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `cityid` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cfg_areas_areaid`(`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区域县区信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cfg_cities
-- ----------------------------
DROP TABLE IF EXISTS `cfg_cities`;
CREATE TABLE `cfg_cities`  (
  `id` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `provinceid` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cfg_cities_cityid`(`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '行政区域地州市信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cfg_provinces
-- ----------------------------
DROP TABLE IF EXISTS `cfg_provinces`;
CREATE TABLE `cfg_provinces`  (
  `id` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '省份信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for cfg_unionpay
-- ----------------------------
DROP TABLE IF EXISTS `cfg_unionpay`;
CREATE TABLE `cfg_unionpay`  (
  `bank_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '总行名称',
  `cityid` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '城市id',
  `bank_branch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支行名称',
  `unionpay_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付联行号',
  PRIMARY KEY (`unionpay_no`) USING BTREE,
  INDEX `idx_cfg_unionpay_cityid_bankcode`(`cityid`, `bank_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '银联行号' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for industry
-- ----------------------------
DROP TABLE IF EXISTS `industry`;
CREATE TABLE `industry`  (
  `id` bigint(10) NOT NULL COMMENT 'ID',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '栏目名',
  `parentid` bigint(10) NOT NULL COMMENT '父栏目',
  `describe` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for merch_charge
-- ----------------------------
DROP TABLE IF EXISTS `merch_charge`;
CREATE TABLE `merch_charge`  (
  `business_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '业务单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '充值商户号',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '渠道',
  `amount` decimal(14, 2) NULL DEFAULT NULL COMMENT '充值金额',
  `order_state` int(2) NULL DEFAULT NULL COMMENT '订单状态',
  `clear_state` int(2) NULL DEFAULT NULL COMMENT '清算状态',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `msg` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '消息提示信息',
  PRIMARY KEY (`business_no`, `merch_no`, `out_channel`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户充值' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for merch_user_sign
-- ----------------------------
DROP TABLE IF EXISTS `merch_user_sign`;
CREATE TABLE `merch_user_sign`  (
  `user_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商户用户标识',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付商户',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付公司',
  `bank_no` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '银行卡号',
  `sign` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '快捷签约',
  `info` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他一些支付信息',
  `acct_name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '持卡人姓名',
  `acct_type` int(2) NULL DEFAULT 0 COMMENT '账户类型0对私 1对公',
  `cert_type` int(2) NULL DEFAULT 1 COMMENT '证件类型 1身份证',
  `cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件号码',
  `phone` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `cvv2` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '信用卡背面cvv2码后三位',
  `valid_date` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '有效期，年月，四位数，例：2112',
  `coll_type` int(2) NULL DEFAULT 1 COMMENT '1-快捷支付 2-代扣扣款',
  `card_type` int(2) NULL DEFAULT 0 COMMENT '银行卡类型0 储蓄卡 1 信用卡',
  `bank_code` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行代码',
  PRIMARY KEY (`user_id`, `merch_no`, `pay_merch`, `pay_company`, `bank_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商户号下的用户签约信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for merchant
-- ----------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant`  (
  `user_id` int(11) NOT NULL COMMENT 'id主键',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户号 JFSH00000001',
  `public_key` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'RSA公钥',
  `crt_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP,
  `status` int(1) NULL DEFAULT 0 COMMENT '状态  1 启用  0禁用',
  `audit_status` int(1) NULL DEFAULT 2 COMMENT '审核状态  1通过  0 待审核  2 不通过',
  `pay_channel_type` int(1) NULL DEFAULT NULL COMMENT '支付通道分类(走哪种分类的通道)',
  `parent_agent` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上级代理商户号   [商户开户信息开始]',
  `merchants_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户名称   [商户联系人信息开始]',
  `contacts` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contacts_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contacts_email` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `contacts_qq` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系人QQ   [商户联系人信息结束]',
  `logo_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户logo地址',
  `coin_switch` json NULL COMMENT '币开关',
  `coin_rate` json NULL COMMENT '币费率(手续费)',
  `cust_rate` json NULL COMMENT '客户费率(商户自己设置)',
  `acp_cny_min` int(6) NULL DEFAULT NULL COMMENT '代付CNY最小值',
  `acp_cny_max` int(10) NULL DEFAULT NULL COMMENT '代付CNY最大值',
  `acp_usdt_min` int(6) UNSIGNED NULL DEFAULT NULL COMMENT '代付USDT最小值',
  `acp_usdt_max` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '代付USDT最大值',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `unique_merch_no`(`merch_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '平台商户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for oa_notify
-- ----------------------------
DROP TABLE IF EXISTS `oa_notify`;
CREATE TABLE `oa_notify`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `type` char(1) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '类型',
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(2000) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '内容',
  `files` varchar(2000) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '附件',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '状态',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '备注信息',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `oa_notify_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '通知通告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for oa_notify_record
-- ----------------------------
DROP TABLE IF EXISTS `oa_notify_record`;
CREATE TABLE `oa_notify_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `notify_id` bigint(20) NULL DEFAULT NULL COMMENT '通知通告ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '接受人',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '阅读标记',
  `read_date` date NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `oa_notify_record_notify_id`(`notify_id`) USING BTREE,
  INDEX `oa_notify_record_user_id`(`user_id`) USING BTREE,
  INDEX `oa_notify_record_read_flag`(`is_read`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '通知通告发送记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_acct_bal
-- ----------------------------
DROP TABLE IF EXISTS `pay_acct_bal`;
CREATE TABLE `pay_acct_bal`  (
  `user_id` int(11) NOT NULL,
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `user_type` int(2) NULL DEFAULT NULL COMMENT '用户类型 0-普通用户 1-资金账号 2-启晗商户 3-启晗商户代理 4-支付商户 5-支付代理',
  `balance` decimal(16, 2) NULL DEFAULT NULL COMMENT '余额(元)',
  `avail_bal` decimal(16, 2) NULL DEFAULT 0.00 COMMENT '可用余额，实际代付比较余额',
  `freeze_bal` decimal(16, 2) NULL DEFAULT NULL COMMENT '冻结金额',
  `total_income` decimal(16, 2) NULL DEFAULT NULL COMMENT '总入账',
  `total_spending` decimal(16, 2) NULL DEFAULT NULL COMMENT '总出账',
  `total_poundage` decimal(16, 2) NULL DEFAULT NULL COMMENT '总手续费',
  `company_pay_avail_bal` json NULL COMMENT '商户在不同的支付公司下的余额分布',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '账号余额表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_audit
-- ----------------------------
DROP TABLE IF EXISTS `pay_audit`;
CREATE TABLE `pay_audit`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户号',
  `audit_type` int(2) NOT NULL COMMENT '审核类型 0代付审核',
  `audit_result` int(2) NULL DEFAULT NULL,
  `auditor` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核人',
  `audit_time` timestamp(0) NULL DEFAULT NULL COMMENT '审核时间',
  `crt_time` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `memo` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `amount` decimal(11, 2) NULL DEFAULT 0.00 COMMENT '到账金额',
  `poundage` decimal(11, 2) NULL DEFAULT 0.00 COMMENT '手续费',
  PRIMARY KEY (`order_no`, `merch_no`, `audit_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付审核' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_bank
-- ----------------------------
DROP TABLE IF EXISTS `pay_bank`;
CREATE TABLE `pay_bank`  (
  `company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付公司',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '支付商户号',
  `card_type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '银行卡类型0-储蓄 1-信用',
  `banks` json NULL COMMENT '银行卡列表',
  PRIMARY KEY (`company`, `card_type`, `pay_merch`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付银行' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_config_company
-- ----------------------------
DROP TABLE IF EXISTS `pay_config_company`;
CREATE TABLE `pay_config_company`  (
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付商户',
  `company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付公司',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '渠道',
  `cost_rate` decimal(6, 4) NULL DEFAULT NULL COMMENT '支付成本费率',
  `cost_rate_unit` int(1) NULL DEFAULT 1 COMMENT '成本手续费 单位 1% 2元',
  `qh_rate` decimal(6, 4) NULL DEFAULT NULL COMMENT '启晗代理费率',
  `qh_rate_unit` int(1) NULL DEFAULT 1 COMMENT '默认手续费 单位 1% 2元',
  `max_pay_amt` int(8) NULL DEFAULT NULL COMMENT '单笔最大支付额',
  `min_pay_amt` int(6) NULL DEFAULT NULL COMMENT '单笔最新支付额',
  `max_fee` decimal(11, 2) NULL DEFAULT 0.00 COMMENT '最大手续费费率',
  `min_fee` decimal(11, 2) NULL DEFAULT NULL,
  `crt_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_period` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `if_close` int(1) NULL DEFAULT NULL COMMENT '是否关闭 0 不关闭， 1 关闭',
  `pay_channel_type` int(2) NULL DEFAULT NULL COMMENT '通道分类',
  `callback_domain` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '回调域名',
  `capital_pool` int(11) NULL DEFAULT NULL COMMENT '资金池 (支付公司开给这个商户号的单日限额)',
  `payment_method` int(1) NULL DEFAULT NULL COMMENT '结算方式D0 T1 D1 T0',
  `clear_ratio` decimal(4, 2) NULL DEFAULT 1.00 COMMENT '清算比例',
  `weight` decimal(4, 2) NULL DEFAULT NULL COMMENT '权重',
  PRIMARY KEY (`pay_merch`, `company`, `out_channel`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付公司通道配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户号',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付商户号',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '渠道编码',
  `title` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标题',
  `product` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品名称',
  `amount` decimal(14, 2) NULL DEFAULT NULL COMMENT '订单金额(元)',
  `currency` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'CNY' COMMENT '币种',
  `order_state` int(2) NULL DEFAULT 3 COMMENT '订单状态',
  `return_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '前台回调地址',
  `notify_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '异步通知地址',
  `req_time` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求时间',
  `user_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户用户标志',
  `bank_no` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行卡号',
  `req_ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求ip',
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注留言信息',
  `business_no` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方支付订单号',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `real_amount` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '实际支付金额',
  `cost_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '成本金额',
  `qh_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '手续费',
  `agent_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '商户代理金额',
  `sub_agent_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '上级代理金额',
  `msg` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '提示消息',
  `clear_state` int(2) NULL DEFAULT 0 COMMENT '0未清算 1清算成功 2清算失败',
  `order_type` int(2) NULL DEFAULT 0 COMMENT '订单类型',
  `payment_method` int(1) NULL DEFAULT NULL COMMENT '结算方式  D0  T1',
  `notice_state` int(1) NULL DEFAULT 0 COMMENT '通知状态   0 未通知  1已通知',
  `clear_amount` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '清算金额',
  PRIMARY KEY (`order_no`, `merch_no`) USING BTREE,
  INDEX `index_pay_order_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_order_acp
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_acp`;
CREATE TABLE `pay_order_acp`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户号',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付商户号',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '渠道编码',
  `title` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `amount` decimal(14, 2) NULL DEFAULT NULL COMMENT '订单金额(元)',
  `currency` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'CNY' COMMENT '币种',
  `order_state` int(2) NULL DEFAULT NULL COMMENT '订单状态',
  `return_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '前台回调地址',
  `notify_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '异步通知地址',
  `req_time` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求时间',
  `user_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户用户标志',
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注留言信息',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `business_no` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方支付订单号',
  `acct_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账户名',
  `bank_no` varchar(22) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行卡号',
  `mobile` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开户手机号',
  `bank_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开户行号',
  `bank_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开户行名称',
  `cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证号码',
  `card_type` int(2) NULL DEFAULT -1 COMMENT '银行卡类型 0 储蓄卡 1 信用卡',
  `acct_type` int(2) NULL DEFAULT -1 COMMENT '账户性质 0 对私 1 对公',
  `msg` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '提示消息',
  `real_amount` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '实际支付金额',
  `cost_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '成本金额',
  `qh_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '启晗代理金额',
  `agent_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '商户代理金额',
  `sub_agent_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '上级代理金额',
  `clear_state` int(2) NULL DEFAULT 0 COMMENT '0未清算 1清算成功 2清算失败',
  `order_type` int(2) NULL DEFAULT 0 COMMENT '订单类型',
  `user_type` int(2) NULL DEFAULT NULL COMMENT '用户类型',
  `notice_state` int(1) NULL DEFAULT 0 COMMENT '通知状态   0 未通知  1已通知',
  PRIMARY KEY (`order_no`, `merch_no`) USING BTREE,
  INDEX `index_pay_order_acp_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代付订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_order_lose
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_lose`;
CREATE TABLE `pay_order_lose`  (
  `business_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '业务单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户号',
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '订单号',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司',
  `amount` decimal(14, 2) NULL DEFAULT NULL COMMENT '金额',
  `qh_amount` decimal(14, 2) NULL DEFAULT 0.00 COMMENT '聚富手续费',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付渠道',
  `order_state` int(2) NULL DEFAULT NULL,
  `msg` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '信息详情',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`business_no`, `merch_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '掉单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_property
-- ----------------------------
DROP TABLE IF EXISTS `pay_property`;
CREATE TABLE `pay_property`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司 0 为系统默认参数',
  `merchantNo` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户号',
  `config_type` int(2) NULL DEFAULT 0 COMMENT ' 0为普通文本 1 密码 2域名Ip 3 文件路径 4文件内容 5 商户号',
  `config_key` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置标识',
  `value` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '值',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称说明',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 283 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支付参数配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_qr_config
-- ----------------------------
DROP TABLE IF EXISTS `pay_qr_config`;
CREATE TABLE `pay_qr_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商户号',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付渠道',
  `account_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收款账号',
  `account_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收款名称',
  `account_phone` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `service_tel` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '客服电话',
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  `qrs` json NULL COMMENT '收款二维码',
  `cost_rate` decimal(6, 4) NULL DEFAULT 0.0000,
  `jf_rate` decimal(6, 4) NULL DEFAULT 0.0000,
  `api_key` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '启晗扫码通道配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_found_acct
-- ----------------------------
DROP TABLE IF EXISTS `record_found_acct`;
CREATE TABLE `record_found_acct`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户号',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额(元)',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额(元)',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `changeUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`order_no`, `merch_no`, `fee_type`, `username`, `profit_loss`) USING BTREE,
  INDEX `inx_r_f_a_gmt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代理商余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_found_avail_acct
-- ----------------------------
DROP TABLE IF EXISTS `record_found_avail_acct`;
CREATE TABLE `record_found_avail_acct`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户号',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额(元)',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额(元)',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `changeUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`order_no`, `merch_no`, `fee_type`, `username`, `profit_loss`) USING BTREE,
  INDEX `inx_r_f_a_gmt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代理商可用余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_merch_avail_bal
-- ----------------------------
DROP TABLE IF EXISTS `record_merch_avail_bal`;
CREATE TABLE `record_merch_avail_bal`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `changeUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `inx_r_m_b_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '聚富商户可用余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_merch_bal
-- ----------------------------
DROP TABLE IF EXISTS `record_merch_bal`;
CREATE TABLE `record_merch_bal`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NOT NULL COMMENT '订单类型',
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  `changeUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`order_no`, `order_type`, `profit_loss`, `merch_no`) USING BTREE,
  INDEX `inx_r_m_b_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '聚富商户余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_pay_merch_avail_bal
-- ----------------------------
DROP TABLE IF EXISTS `record_pay_merch_avail_bal`;
CREATE TABLE `record_pay_merch_avail_bal`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '启晗商户',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付商户号',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `inx_r_m_b_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '第三方支付可用余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for record_pay_merch_bal
-- ----------------------------
DROP TABLE IF EXISTS `record_pay_merch_bal`;
CREATE TABLE `record_pay_merch_bal`  (
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单号',
  `merch_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '聚富商户',
  `fee_type` int(2) NOT NULL COMMENT '费用类型',
  `order_type` int(2) NULL DEFAULT NULL COMMENT '订单类型',
  `pay_merch` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付商户号',
  `pay_company` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付公司',
  `out_channel` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '渠道',
  `before_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动前金额(元)',
  `tran_amt` decimal(12, 2) NULL DEFAULT NULL COMMENT '变动金额',
  `after_amt` decimal(16, 2) NULL DEFAULT NULL COMMENT '变动后金额',
  `profit_loss` int(2) NOT NULL COMMENT '账户余额盈亏 0就是出账 1 为入账',
  `crt_date` int(11) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`order_no`, `merch_no`, `fee_type`, `profit_loss`) USING BTREE,
  INDEX `inx_r_m_b_crt_date`(`crt_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '第三方支付余额流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `config_item` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置项',
  `config_value` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `parent_item` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '父类配置',
  `is_close` smallint(4) NULL DEFAULT NULL COMMENT '是否关闭    -1不需要开启这功能 0开启   1关闭',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统参数配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '上级部门ID，一级部门为0',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `order_num` int(11) NULL DEFAULT NULL COMMENT '排序',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除  -1：已删除  0：正常',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '部门管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '标签名',
  `value` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '数据值',
  `type` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '类型',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '描述',
  `sort` decimal(10, 0) NULL DEFAULT NULL COMMENT '排序（升序）',
  `parent_id` bigint(64) NULL DEFAULT 0 COMMENT '父级编号',
  `create_by` int(64) NULL DEFAULT NULL COMMENT '创建者',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(64) NULL DEFAULT NULL COMMENT '更新者',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '备注信息',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sys_dict_value`(`value`) USING BTREE,
  INDEX `sys_dict_label`(`name`) USING BTREE,
  INDEX `sys_dict_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 121 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` int(11) NULL DEFAULT NULL COMMENT '文件类型',
  `url` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'URL地址',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 406 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '文件上传' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户操作',
  `time` int(11) NULL DEFAULT NULL COMMENT '响应时间',
  `method` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求参数',
  `ip` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `gmt_create` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2511 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) NULL DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单URL',
  `perms` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
  `type` int(11) NULL DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
  `icon` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `order_num` int(4) NULL DEFAULT NULL COMMENT '排序',
  `gmt_create` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 230 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '菜单管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `creator` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `role_sign` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色标识',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `user_id_create` int(11) NULL DEFAULT NULL COMMENT '创建用户id',
  `gmt_create` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11535 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色与菜单对应关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `cron_expression` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'cron表达式',
  `method_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务调用的方法名',
  `is_concurrent` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务是否有状态',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务描述',
  `update_by` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `bean_class` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务执行时调用哪个类的方法 包名+类名',
  `create_date` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `job_status` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务状态',
  `job_group` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务分组',
  `update_date` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `spring_bean` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Spring bean',
  `job_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '任务名',
  `params` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 16 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统任务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '显示名称',
  `password` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `fund_password` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `dept_id` int(11) NULL DEFAULT NULL,
  `email` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(2) NULL DEFAULT NULL COMMENT '状态 0:禁用，1:正常',
  `user_id_create` int(11) NULL DEFAULT NULL COMMENT '创建用户id',
  `gmt_create` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `user_type` int(2) NULL DEFAULT NULL COMMENT '用户类型 0-普通用户 1-资金账号 2-启晗商户 3-启晗商户代理 4-支付商户 5-支付代理',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 209 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 241 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户与角色对应关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_bank
-- ----------------------------
DROP TABLE IF EXISTS `user_bank`;
CREATE TABLE `user_bank`  (
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商户用户标识',
  `bank_no` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '银行卡号',
  `bank_code` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行代码',
  `card_type` int(2) NULL DEFAULT 0 COMMENT '银行卡类型0 储蓄卡 1 信用卡',
  `acct_name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '持卡人姓名',
  `acct_type` int(2) NULL DEFAULT 0 COMMENT '账户类型0对私 1对公',
  `cert_type` int(2) NULL DEFAULT 1 COMMENT '证件类型 1身份证',
  `cert_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件号码',
  `phone` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `unionpay_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付联行号',
  `bank_branch` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支行名称',
  `cvv2` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '信用卡背面cvv2码后三位',
  `valid_date` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '有效期，年月，四位数，例：2112',
  `info` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '其他一些支付信息',
  `city` int(11) NULL DEFAULT NULL,
  `province` int(11) NULL DEFAULT NULL,
  `bankProvince` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bankCity` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`username`, `bank_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户银行卡' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
