package com.qh.paythird.test;


import org.springframework.stereotype.Service;

/**
 *
 * 支付测试对接 cbd 平台
 */

@Service
public class ZhiFuService {
//
//    private  static final Logger logger = LoggerFactory.getLogger(ZhiFuService.class);
//
//    @Autowired
//    private MerchantService merchantService;
//
//    @Autowired
//    private PayService payService;
//
//    @Autowired
//    PayOrderDao payOrderDao;
//
//    /**
//     * 下单路径
//     */
//    public final static String urlup = "http://www.long-pay.com/api/index/order";
//
//    /**
//     * @Description 支付发起
//     * @param order
//     * @return
//     */
//    public R order(Order order) {
//        logger.info("测试钱包支付 开始------------------------------------------------------");
//        try {
//            /**
//             *
//             * 支付宝支付（H5）
//             * 支付宝扫码支付
//             * 支付宝刷卡支付
//             * 微信支付（H5）
//             * 微信扫码支付
//             * 微信刷卡支付
//             * 网银跳转
//             *
//             */
//
//            if (OutChannel.wx.name().equals(order.getOutChannel())) {
//                //微信扫码支付
////                return order_wx(order);
//            }
//
//            if (OutChannel.wxposs.name().equals(order.getOutChannel())) {
//                //微信刷卡支付
////                return order_wxposs(order);
//            }
//
//            if (OutChannel.wap.name().equals(order.getOutChannel())) {
//                //微信H5支付
//				return order_wap(order);
//            }
//
//            if (OutChannel.wy.name().equals(order.getOutChannel())) {
//                //网银支付
////                return order_wy(order);
//            }
//
//			if (OutChannel.ali.name().equals(order.getOutChannel())) {
//				//支付宝支付
////				return order_ali(order);
//			}
//
//            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
//                //支付宝H5支付
////				return order_aliwap(order);
//            }
//
//            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
//                //支付宝刷卡支付
////				return order_aliposs(order);
//            }
//
//            logger.error("测试钱包支付 不支持的支付渠道：{}", order.getOutChannel());
//            return R.error("不支持的支付渠道");
//        } finally {
//            logger.info("测试钱包支付 结束------------------------------------------------------");
//        }
//    }
//
//    /**
//     *
//     * 微信H5支付
//     */
//     private R order_wap (Order order){
//
//            return pay(order);
//     }
//
//
//    /**
//     * 支付数据
//     */
//    private R pay(Order order ){
//        logger.info("支付数据 ：" +order.toString());
//        try {
//            HashMap<String, String> map = new HashMap<String,String>() ;
//
//            String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//            //商户号
//            map.put("user_id", "335");
//            //订单号
//            map.put("remark", order.getOrderNo());
////            map.put("remark", reqTime + new Random().nextInt(10000));
//            //交易金额
//            map.put("pay_amount",order.getAmount().toString());
//            //支付渠道
//            map.put("channel_id", "56");
//            //订单标题 & 产品编号
//            map.put("product_id", "901");
//            System.out.println("product----------"+order.getProduct());
//            //前端返回地址
//            map.put("return_url", "www.baidu.com");
//            //后台通知地址
//            map.put("notify_url", "http://47.102.149.20:8181/pay/notify/"+"yuzhifu"+"/"+order.getUserId()+"/"+order.getOrderNo());
//
//
//            String contStr = http_build_query(map);
//
//            logger.info("请求source:" + contStr);
//
//            String context = Base64.encode(contStr.getBytes());
//
//            byte[] decode = Base64.decode(context);
//            System.out.println("decode : " + new String(decode));
//
//            System.out.println("context : " + context);
//            String sign = Md5Util.sign(context,"d53b8fc50f6e41223f6f85b1e7691e40","utf-8");
//            String md5 = Md5.getMd5ofStr(context+"d53b8fc50f6e41223f6f85b1e7691e40");
//
//
//            Map<String,String > jomap = new HashMap<>();
//            jomap.put("sign", sign);
//            jomap.put("data", context);
//            jomap.put("user_id","335");
//
//            logger.info("请求参数：{}", jomap);
//            logger.info("请求参数：{}", jomap.toString());
//
//            Map result = HttpClientUtil.doPostQueryCmd(urlup,jomap);
////            Map result = HttpClientUtil.doPostQueryCmd("",jomap);
//            System.out.println("result : " + result.toString());
//            String content = result.get("content").toString();
//            System.out.println("content : " + content);
//            if(StringUtils.isBlank(result.toString())){
//                return R.error("支付返回参数为空");
//            }
//            JSONObject json = JSON.parseObject(content);
//            logger.info("返回数据 ： " +json.get("data"));
//            JSONObject jsondata = JSON.parseObject(json.get("data").toString());
//            String resCode = json.get("code").toString();
//            if("000000".equals(resCode)){
//                return R.error(json.get("msg").toString());
//            }else{
////                System.out.println("zfOrderNo------"+order.getOrderNo());
////                RedisUtil.setOrder(order);
////                Order order1=RedisUtil.getOrder(order.getMerchNo(),order.getOrderNo());
////                System.out.println("RedisUtil 支付OrderNo---------"+order1.getOrderNo());
//               int ints = payOrderDao.save(order);
//               logger.info("ints : " + ints);
//                Map<String,String> data = new HashMap<>();
//                //拿出跳转地址返回
//                logger.info("获取测试返回路径 ：" +jsondata.get("qrCode").toString() );
//                logger.info("获取测试返回路径 ：" +content );
//                data.put(PayConstants.web_qrcode_url,jsondata.get("qrCode").toString());
//                return R.okData(content);
//            }
//        } catch (Exception e){
//            logger.error("测试支付 异常：" + e.getMessage());
//            e.printStackTrace();
//            return R.error("支付异常");
//        } finally {
//            logger.info("测试支付");
//        }
//    }
//
//
//    /**
//     * 后台返回地址
//     * @param
//     * @return
//     */
//    public R notify(Order order, HttpServletRequest request, String requestBody) {
//        Map<String,String> requestMap=new HashMap<String,String>();
//        requestMap.put("user_id",request.getParameter("user_id"));
//        requestMap.put("trade_no",request.getParameter("trade_no"));
//        requestMap.put("transaction_money",request.getParameter("transaction_money"));
//        requestMap.put("datetime",request.getParameter("datetime"));
//        requestMap.put("transaction_id",request.getParameter("transaction_id"));
//        requestMap.put("returncode",request.getParameter("returncode"));
//        requestMap.put("remark",request.getParameter("remark"));
//        requestMap.put("sign",request.getParameter("sign"));
//        String context=http_build_query(requestMap);
//       /* Map<String, String> requestMap = RequestUtils.getAllRequestParamStream(request);
//        logger.info("mofangceo支付回调结果：{}", requestMap);
//        String sign = requestMap.get("sign");
//        requestMap.remove("sign");
//        String data = Tools.getUrlParamsByMap(new TreeMap<>(requestMap))
//                .replaceAll("\\\\/", "/").replaceAll("\\\\\\\\", "\\\\");*/
//       String sign=request.getParameter("sign");
//        /*try {
//            logger.info("verify====>" + Md5Util.verify(context,sign,"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG5Tvmz3O8Tutx0QmfTqWE12fRBhqxCavY0eSmqRq4KCHmNekNHTBRkQS7tW+Rnt89YjRiaYwDJHDKcopdiApztQ5E+qqnuHcVluB42rj1omA84g4kwbt5h44wEe2x9ERKCQI1JnSERJ+Z7Ku3fNV0xCslLQb6bRi5izJ6gHKL+wIDAQAB","utf-8"));
//        } catch (SignatureException e) {
//            e.printStackTrace();
//            logger.error("mofangceo支付回调验签返回失败");
//            return R.error("支付回调验签返回失败");
//        }*/
//        System.out.println(context);
//        logger.info("verify====>" + Md5Util.verify(context,sign,"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG5Tvmz3O8Tutx0QmfTqWE12fRBhqxCavY0eSmqRq4KCHmNekNHTBRkQS7tW+Rnt89YjRiaYwDJHDKcopdiApztQ5E+qqnuHcVluB42rj1omA84g4kwbt5h44wEe2x9ERKCQI1JnSERJ+Z7Ku3fNV0xCslLQb6bRi5izJ6gHKL+wIDAQAB","utf-8"));
//        String amount = request.getParameter("transaction_money");
//        if (ParamUtil.isNotEmpty(amount)) {
//            order.setRealAmount(new BigDecimal(amount));
//            System.out.println("realAmount-----------------"+order.getRealAmount());
//        }
//        String pay_status = requestMap.get("returncode").toString();
//        String code="fail";
//        String msg="失败";
//        if(MofangConst.pay_status_succ.equals(pay_status)){
//            order.setOrderState(1);
//            payOrderDao.update(order);
//            payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
//            code="success";
//            msg="成功";
//        }
//        Map jomap=new HashMap();
//        jomap.put("orderNo",order.getOrderNo());
//        jomap.put("code",code);
//        jomap.put("msg",msg);
//        Map<String, Object> result=HttpClientUtil.doPostQueryCmd(order.getNotifyUrl(),jomap);
//        System.out.println("result------------"+result);
//        return R.ok();
//    }
//
//    static String http_build_query(Map<String, String> array){
//        String reString = "";
//        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
//        Iterator it = array.entrySet().iterator();
//        while (it.hasNext()){
//            Map.Entry<String,String> entry =(Map.Entry) it.next();
//            String key = (String) entry.getKey();
//            Object value =entry.getValue();
//            System.out.println(value);
//            reString += key+"="+value+"&";
//        }
//        reString = reString.substring(0, reString.length()-1);
//        System.out.println("reString : " + reString);
//        //将得到的字符串进行处理得到目标格式的字符串
//        reString = java.net.URLEncoder.encode(reString);
//        System.out.println("reString : " + reString);
//        reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
//        return reString;
//    }
//
//}
}


