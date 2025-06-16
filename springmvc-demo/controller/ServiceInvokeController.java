
@RestController("/")
public class FooController {



//    {
//        "method": "addPrintTask",
//        "class": "com.dover.template.PrintTemplateAdminService",
//        "argsClassList": [
//        "com.dover.PrintTaskAddReq"
//  ],
//        "args": [
//        {
//            "businessOrderNo": "EWD202503031604239436",
//            "businessOrderCode": "warehouseExecuteDemand",
//            "businessOrderNo": "EWL202410151033126389",
//            "businessOrderCode": "transportWarehouseExecute",
//            "businessOrderNo": "EQT2024031215321018473439",
//            "businessOrderCode": "goodsQualityControl",
//            "tenantId": 429099963479877,
//            "tenantCode": "aaa",
//            "userId": 429102657396421,
//            "userName": "账号",
//            "menuType": 0
//        }
//  ],
//        "1": ""
//    }

    /**
     * 调用任意spring singleton 的方法
     */
    @Resource
    private ApplicationContext applicationContext;
    @PostMapping(value = "/dover")
    public void dover(@RequestBody JSONObject param) throws ClassNotFoundException {
        Object[] args = param.getObject("args", Object[].class);
        Class<?> clazz = Class.forName(param.getString("class"));
        Object serviceBean = applicationContext.getBean(clazz);
        String[] argsClassStrList = param.getObject("argsClassList", String[].class);
        Class[] argsClassList = new Class[argsClassStrList.length];
        for (int i = 0; i < argsClassStrList.length; i++) {
            argsClassList[i] = Class.forName(argsClassStrList[i]);
        }
        Method method = ReflectUtil.getMethod(clazz, param.getString("method"), argsClassList);
        Object invoke = ReflectUtil.invoke(serviceBean, method, args);
        System.out.println(invoke);
    }
}