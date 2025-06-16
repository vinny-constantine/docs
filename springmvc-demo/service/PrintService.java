
@Slf4j
@Service
public class PrintBusinessTemplateAdminServiceImpl implements PrintBusinessTemplateAdminService {




    public static void main(int[] args) {
        //spring spel 根据上下文计算
        JSON context = new JSONObject();

        String serializedExpressions = "a * b";

        Expression expression = expressionMap.containsKey(serializedExpressions)
            ? expressionMap.get(serializedExpressions)
            : spelExpressionParser.parseExpression(serializedExpressions);
        expressionMap.putIfAbsent(serializedExpressions, expression);
        Object serializedValue = expression.getValue(context);
        context.put(entry.getKey(), serializedValue);
    }





    @Override
    public OmsResult<PrintDataQueryDto> queryPrintData(PrintDataQueryReq printDataQueryReq) {
        //TODO 根据生态、应用编码（打印）查询生态业务配置
        ErpEcologicalManagementBean erpEcologicalManagementBean = erpEcologicalManagementBeanMapper.selectByCondition(ErpEcologicalManagementSelectReq.builder()
            .applicationSystemCode("")//TODO 打印系统的编码
            .ecosystemType(printDataQueryReq.getEcosystemType())
            .build());
        if (erpEcologicalManagementBean == null) {
            return OmsResult.error("未查询到打印系统的生态配置");
        }
        //业务单据编码查询单据配置
        List<ErpEcologicalManagementDetailBean> erpEcologicalManagementDetailBeanList = erpEcologicalManagementDetailBeanMapper.selectByCondition(ErpEcologicalManagementDetailBean
            .builder()
            .ecologicalId(erpEcologicalManagementBean.getId())
            .businessCode(printDataQueryReq.getBusinessOrderCode())
            .build());
        if (CollUtil.isEmpty(erpEcologicalManagementDetailBeanList)) {
            return OmsResult.error("未查询到打印系统的业务单据配置");
        }
        //根据单据配置查询所有展示字段、序列化方式等
        ErpEcologicalManagementDetailBean erpEcologicalManagementDetailBean = erpEcologicalManagementDetailBeanList.get(0);
        List<ErpEcologicalManagementDetailFieldsDto> erpEcologicalManagementDetailFieldsDtoList = erpEcologicalManagementDetailFieldsBeanMapper.selectDtoByCondition(ErpEcologicalManagementDetailFieldsBean
            .builder()
            .ecologicalDetailId(erpEcologicalManagementDetailBean.getId())
            .businessCode(erpEcologicalManagementDetailBean.getBusinessCode())
            .build());
        if (CollUtil.isEmpty(erpEcologicalManagementDetailFieldsDtoList)) {
            return OmsResult.error("未查询到打印系统的业务单据字段配置");
        }
        //查询业务单据注册配置
        ErpBusinessDataRegistrationBean erpBusinessDataRegistrationBean = erpBusinessDataRegistrationMapper.queryErpBusinessDataRegistrationByEcosystemTypeAndBusiness(
            printDataQueryReq.getEcosystemType(),
            null,
            erpEcologicalManagementDetailBean.getBusinessCode());
        if (erpBusinessDataRegistrationBean == null) {
            return OmsResult.error("未查询到打印系统的业务单据注册配置");
        }
        String executionMethod = erpBusinessDataRegistrationBean.getExecutionMethod();
        if (StringUtils.isEmpty(executionMethod)) {
            return OmsResult.error("未查询到打印系统的业务单据注册配置的执行方法");
        }
        //根据业务单据配置、业务单据号查询单据打印数据
        OmsResult<Object> omsResult = tryInvokeExecutionMethod(executionMethod, printDataQueryReq.getReq());
        if (omsResult.isNg()) {
            log.error("执行方法={}执行失败, 请检查执行方法和参数, args={}", executionMethod, printDataQueryReq.getReq());
            return OmsResult.error(omsResult.getMsg());
        }
        //组装展示字段及打印数据,并按配置序列化
        OmsResult<JSONArray> printDataResult = serializePrintData(erpEcologicalManagementDetailFieldsDtoList, omsResult.getData());
        if (printDataResult.isNg()) {
            log.error("打印数据序列化失败, 请检查打印数据字段配置, fields={}, data={}", JSON.toJSONString(erpEcologicalManagementDetailFieldsDtoList), JSON.toJSONString(omsResult.getData()));
            return OmsResult.error(printDataResult.getMsg());
        }
        //处理打印通用逻辑, 如设置headList
        handlePrintData(printDataResult.getData());
        return OmsResult.success(PrintDataQueryDto.builder()
            .businessOrderCode(printDataQueryReq.getBusinessOrderCode())
            .dataList(printDataResult.getData())
            .build());
    }
    
    /**
     * 组装展示字段及打印数据,并按配置序列化
     */
    private OmsResult<JSONArray> serializePrintData(List<ErpEcologicalManagementDetailFieldsDto> fieldsList, Object data) {
        //将字段按parentId分组
        Map<Long, List<ErpEcologicalManagementDetailFieldsDto>> parentIdAndFieldListMap = fieldsList.stream().collect(Collectors.groupingBy(ErpEcologicalManagementDetailFieldsDto::getParentId));
        //组装字段层级
        Map<String, ErpEcologicalManagementDetailFieldsDto> topFieldMap = new HashMap<>();
        fieldsList.forEach(item -> {
            List<ErpEcologicalManagementDetailFieldsDto> childList = parentIdAndFieldListMap.get(item.getId());
            if (CollUtil.isNotEmpty(childList)) {
                item.setChildren(childList);
                topFieldMap.put(item.getFieldCode(), item);
            }
        });
        //尝试解析data到单据层级
        // 1.如果data是JSONArray, 则直接视为单据单据列表
        // 2.如果data是JSONObject, 则尝试将data转为OmsResult, 获取key=data的数据, 并执行1判断
        // 3.将data先序列化为String, 再反序列化为JSON, 再做1、2判断
        //获取业务单据列表
        JSONArray bizOrderList = extractDataList(topFieldMap.keySet(), data);
        if (CollUtil.isNotEmpty(bizOrderList)) {
            SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
            serializeField(spelExpressionParser, bizOrderList, topFieldMap);
        }
        return OmsResult.success(bizOrderList);
    }

    private static void serializeField(SpelExpressionParser spelExpressionParser, JSONArray currentBizOrderList
        , Map<String, ErpEcologicalManagementDetailFieldsDto> currentFieldMap) {
        //遍历该层对象列表
        for (int idx = 0; idx < currentBizOrderList.size(); idx++) {
            JSONObject json = currentBizOrderList.getJSONObject(idx);
            if (json != null && CollUtil.isNotEmpty(currentFieldMap)) {
                //遍历该层对象的所有字段
                for (Map.Entry<String, ErpEcologicalManagementDetailFieldsDto> entry : currentFieldMap.entrySet()) {
                    //获取该字段的字段值
                    Object value = json.get(entry.getKey());
                    //获取单据配置的序列化表达式
                    ErpEcologicalManagementDetailFieldsDto erpEcologicalManagementDetailFieldsDto = entry.getValue();
                    //序列化表达式不为空, 优先执行序列化
                    if (erpEcologicalManagementDetailFieldsDto != null) {
                        String serializedExpressions = erpEcologicalManagementDetailFieldsDto.getSerializedExpressions();
                        if (StringUtils.isNotBlank(serializedExpressions)) {
                            try {
                                //以该层对象为上下文, 执行序列化表达式
                                Expression expression = expressionMap.containsKey(serializedExpressions)
                                    ? expressionMap.get(serializedExpressions)
                                    : spelExpressionParser.parseExpression(serializedExpressions);
                                expressionMap.putIfAbsent(serializedExpressions, expression);
                                Object serializedValue = expression.getValue(json);
                                json.put(entry.getKey(), serializedValue);
                                log.info("执行字段={}的序列化表达式成功", entry.getKey());
                            } catch (Exception e) {
                                log.error("执行字段={}的序列化表达式={}失败, 请检查表达式, data={}", entry.getKey(), serializedExpressions, json, e);
                            }
                        } else if (CollUtil.isNotEmpty(erpEcologicalManagementDetailFieldsDto.getChildren()) && value instanceof JSONArray) {
                            log.info("该字段为集合类型, 递归执行下一层");
                            JSONArray nextBizOrderList = (JSONArray) value;
                            Map<String, ErpEcologicalManagementDetailFieldsDto> nextFieldMap = erpEcologicalManagementDetailFieldsDto.getChildren().stream().collect(Collectors.toMap(ErpEcologicalManagementDetailFieldsDto::getFieldCode, item -> item));
                            serializeField(spelExpressionParser, nextBizOrderList, nextFieldMap);
                        }
                    }
                }
            }
        }
    }

    /**
     * 提取出匹配的单据集合数据
     */
    private static JSONArray extractDataList(Set<String> fieldCodeSet, Object result) {
        if (result instanceof JSONArray) {
            return matchAndGetDataList(fieldCodeSet, (JSONArray) result);
        } else if (result instanceof JSONObject) {
            log.info("待提取的数据是JSONObject, 尝试转为OmsResult, 再获取其data字段");
            JSONObject jsonData = (JSONObject) result;
            if (jsonData.containsKey("data") && jsonData.get("data") != null) {
                result = jsonData.get("data");
            } else {
                log.error("待提取的数据是JSONObject, 但未找到data字段, 返回空集合");
                return new JSONArray();
            }
        } else if (result instanceof String) {
            log.info("待提取的数据是String类型, 反序列化为JSON");
            if (StringUtils.isNotBlank(result.toString())) {
                try {
                    result = JSON.parseObject(result.toString());
                } catch (Exception e) {
                    try {
                        log.error("待提取的数据是String类型, 反序列化为JSONObject失败, 尝试反序列化为JSONArray");
                        result = JSON.parseArray(result.toString());
                    } catch (Exception err) {
                        log.error("待提取的数据是String类型, 反序列化为JSONObject和JSONArray均失败, 返回空集合");
                        return new JSONArray();
                    }
                }
            } else {
                log.error("待提取的数据是String类型, 但为空串, 反序列化为JSON失败, 返回空集合");
                return new JSONArray();
            }
        } else {
            log.info("待提取的数据是其他类型, 先序列化为String, 再反序列化为JSON");
            result = JSON.parseObject(JSON.toJSONString(result));
        }
        return extractDataList(fieldCodeSet, result);
    }

    private static JSONArray matchAndGetDataList(Set<String> fieldCodeSet, JSONArray jsonArrayData) {
        log.info("待提取的数据是JSONArray, 获取其首个元素与单据字段列表进行匹配");
        if (!jsonArrayData.isEmpty()) {
            JSONObject json = jsonArrayData.getJSONObject(0);
            if (CollUtil.containsAny(fieldCodeSet, json.keySet())) {
                log.info("匹配成功, 其首个元素与单据字段列表相匹配");
//                return jsonArrayData.toJavaList(JSONObject.class);
                return jsonArrayData;
            }
        }
        log.info("匹配失败, 其首个元素与单据字段列表不匹配");
        return new JSONArray();
    }

    /**
     * 处理打印通用逻辑, 如设置headList
     */
    private void handlePrintData(JSONArray data) {
        if (CollUtil.isEmpty(data)) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            JSONObject datum = data.getJSONObject(i);
            if (!datum.containsKey(StringConstant.PRINT_HEAD_LIST)) {
                JSONObject newDatum = new JSONObject();
                datum.entrySet().stream()
                    .filter(entry -> !ClassUtils.isAssignableValue(JSONArray.class, entry.getValue()))
                    .forEach(entry -> newDatum.put(entry.getKey(), entry.getValue()));
                datum.put(StringConstant.PRINT_HEAD_LIST, Lists.newArrayList(newDatum));
            } else {
                log.info("打印单据中已包含headList, 忽略");
            }
        }
    }

    private OmsResult<Object> tryInvokeExecutionMethod(String executionMethod, JSON req) {
        if (StringUtils.isEmpty(executionMethod)) {
            return OmsResult.error("未查询到打印系统的业务单据注册配置的执行方法, 请联系技术人员配置");
        }
        if (executionMethod.startsWith(FilePrefixConstant.HTTP)) {
            return tryInvokeExecutionMethodByRemoteApi(executionMethod, req);
        } else {
            return tryInvokeExecutionMethodByLocalService(executionMethod, req);
        }
    }

    /**
     * executionMethod 可以是远程方法, 如"http://localhost:8080/oms/oms-order/queryTradeOrderGoods"
     */
    private OmsResult<Object> tryInvokeExecutionMethodByRemoteApi(String executionMethod, JSON req) {
        JSONObject result = restTemplateService.postForJavaType(executionMethod, req, new TypeReference<JSONObject>() {
        });
        return OmsResult.success(result);
    }

    /**
     * executionMethod 也可以是本地service方法, 如"com.zczy.scm.oms.service.order.inter.TradeOrderQueryService.queryTradeOrderGoods"
     */
    private OmsResult<Object> tryInvokeExecutionMethodByLocalService(String executionMethod, JSON req) {
        try {
            log.info("执行打印系统业务单据注册配置的执行方法: {}", executionMethod);
            int lastIndexOfDot = executionMethod.lastIndexOf(".");
            String executionClassName = executionMethod.substring(0, lastIndexOfDot);
            String executionMethodName = executionMethod.substring(lastIndexOfDot + 1);
            Class<?> serviceClass = Class.forName(executionClassName);
            Object serviceBean = applicationContext.getBean(serviceClass);
            log.info("执行打印系统业务单据注册配置的执行方法 serviceBean: {}", serviceBean.getClass());
            return OmsResult.success(ReflectUtil.invoke(serviceClass, serviceBean, executionMethodName, req));
        } catch (Exception e) {
            log.error("执行打印系统业务单据注册配置的执行方法失败", e);
            return OmsResult.error("查询打印业务数据失败");
        }
    }
}
