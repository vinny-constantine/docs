package com.dover.demo.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dover.demo.entity.QueryTradeIntegrationInfoResp;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author dover
 * @date 2025-04-16
 */
public class SpelUtil {
    public static void main(String[] args) {
        QueryTradeIntegrationInfoResp data = getData();
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        String expressionString = "#data?.tradeIntegrationGoodsList[0]?.tradeIntegrationGoodsAttributeList?.![(#this.attributeName?:'') + '=' + (#this.valueInfo?:'')].stream().collect(T(java.util.stream.Collectors).joining(';'))";
        Expression expression = spelExpressionParser.parseExpression(expressionString);
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        standardEvaluationContext.setVariable("data", data);
        String value = expression.getValue(standardEvaluationContext, String.class);
        System.out.println(value);
//        ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
//        String[] parameterNames = parameterNameDiscoverer.getParameterNames(addConfigMatchStrategy);

//        if(parameterNames != null && parameterNames.length > 0) {
//        }
    }
}