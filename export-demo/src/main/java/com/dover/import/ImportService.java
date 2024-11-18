
@Slf4j
@Service
public class GroupDriverImportListenerFactoryImpl implements GroupDriverImportListenerFactory {

    @Resource
    private DispatchGroupDriverMapper dispatchGroupDriverMapper;

    @Override
    public ReadListener<GroupDriverExcelDto> build(GroupDriverUploadReqDto workTimeItemImportReqDto) {
        return new GroupDriverImportListener(workTimeItemImportReqDto);
    }


    private class GroupDriverImportListener implements ReadListener<GroupDriverExcelDto> {

        private final Map<String, DispatchGroupDriverModel> dataMap = new HashMap<>();

        private final List<String> exceptionMsgList = new ArrayList<>();

        private final Date now = new Date();

        private final AtomicBoolean hasError = new AtomicBoolean(false);

        private GroupDriverUploadReqDto groupDriverUploadReqDto;

        public GroupDriverImportListener(GroupDriverUploadReqDto groupDriverUploadReqDto) {
            this.groupDriverUploadReqDto = groupDriverUploadReqDto;
        }
        
        @Override
        @SneakyThrows
        public void invoke(GroupDriverExcelDto data, AnalysisContext context) {
            ReadRowHolder readRowHolder = context.readRowHolder();
            //从2开始，第一行为表头不计入
            Integer rowIndex = readRowHolder.getRowIndex();
            if (rowIndex <= 1) {
                if (rowIndex == 1) {//校验表头是否被修改
                    if (!"司机姓名".equals(data.getDriverName()) || !"司机手机号码".equals(data.getMobile())) {
                        hasError.set(true);
                        exceptionMsgList.add("请勿修改模板");
                    }
                }
                return;
            }
            String driverName = data.getDriverName();
            String mobile = data.getMobile();
            //行数校验，最大200行，前两行不算
            if (rowIndex > 201) {
                hasError.set(true);
                exceptionMsgList.add("每次导入最多不能超过200条");
            }
            StringBuilder currentRowException = new StringBuilder();
            
            if (StringUtils.isBlank(driverName)) {
                currentRowException.append("司机姓名不能为空").append("|");
                driverName = "";
            } else if (driverName.length() > 50) {
                currentRowException.append("司机姓名不能超过50个字符").append("|");
            }
            if (StringUtils.isBlank(mobile)) {
                currentRowException.append("手机号不能为空").append("|");
                mobile = "";
            } else if (!mobile.matches(RegexConstant.MOBILE_REGEX)) {
                currentRowException.append("手机号格式不正确").append("|");
            }
            //校验手机号是否重复
            for (Map.Entry<String, DispatchGroupDriverModel> entry : dataMap.entrySet()) {
                if (entry.getKey().equals(mobile)) {
                    Long lastRowIdx = entry.getValue().getDriverId();
                    currentRowException.append(String.format("与第%s行手机号重复", lastRowIdx)).append("|");
                }
            }
            
            if (currentRowException.length() > 0) {
                //设置是否存在excel解析异常
                hasError.set(true);
                String rowException = currentRowException.substring(0, currentRowException.length() - 1);
                exceptionMsgList.add(driverName + "," + mobile + "," + rowException);
            } else {
                exceptionMsgList.add(driverName + "," + mobile + ",");
            }
            
            DispatchGroupDriverModel driverModel = new DispatchGroupDriverModel();
            //注意，该临时ID仅用于后续方便报错，不会保存至数据库中
            driverModel.setDriverId(rowIndex.longValue());
            if (!hasError.get()) {// 当前行没有异常信息时，才缓存解析的数据内容
                driverModel.setDriverName(driverName);
                driverModel.setMobile(Long.parseLong(mobile));
                driverModel.setGroupId(groupDriverUploadReqDto.getGroupId());
                driverModel.setCreatedBy(groupDriverUploadReqDto.getOperateUserId());
                driverModel.setCreatedByName(groupDriverUploadReqDto.getOperateRealName());
                driverModel.setCreatedTime(now);
                driverModel.setLastUptBy(groupDriverUploadReqDto.getOperateUserId());
                driverModel.setLastUptByName(groupDriverUploadReqDto.getOperateRealName());
                driverModel.setLastUptTime(now);
                driverModel.setDeleteFlag(0);
            }
            if (StringUtils.isNotBlank(mobile)) {
                dataMap.put(mobile, driverModel);
            }
        }

        public void doAfterAllAnalysed(AnalysisContext context) {
            //校验同一群组下手机号是否与数据库中司机手机号重复
            List<Long> mobileList = dataMap.keySet().stream().filter(NumberUtils::isParsable).map(Long::parseLong).collect(Collectors.toList());
            List<Long> repeatedMobileList = dispatchGroupDriverMapper.queryRepeatedMobileByGroupIdAndMobileList(groupDriverUploadReqDto.getGroupId(), mobileList);
            if (CollectionUtils.isNotEmpty(repeatedMobileList)) {
                for (Long mobile : repeatedMobileList) {
                    DispatchGroupDriverModel driverModel = dataMap.get(mobile.toString());
                    int msgIdx = (int) (driverModel.getDriverId() - (exceptionMsgList.contains("请勿修改模板") ? 1 : 2));
                    String errMsg = exceptionMsgList.get(msgIdx);
                    exceptionMsgList.set(msgIdx, (errMsg.endsWith(",") ? errMsg : errMsg + "|") + "手机号与已有司机手机号重复");
                    hasError.set(true);
                }
            }
            //若有异常信息
            if (hasError.get()) {
                log.info("===========导入司机列表解析异常===========");
                throw new NestedException(DOErrorMsgEnum.IMPORT_AND_PARSE_DISPATCH_GROUP_DRIVER_LIST_EXCEPTION, exceptionMsgList);
            }
            //只读第一页
            if (context.readSheetHolder().getSheetNo() == 0 && !dataMap.isEmpty()) {
                //删除原有数据
                log.info("待保存作业时间明细记录共[{}]条", dataMap.size());
                int cnt = dispatchGroupDriverMapper.insertBulk(dataMap.values());
                log.info("实际保存作业时间明细记录[{}]条", cnt);
                log.info("===========司机群组groupId=[{}]解析完成===========", groupDriverUploadReqDto.getGroupId());
            }
        }
    }

    
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class GroupDriverUploadReqDto implements Serializable {

    //群组ID
    @NotNull(message = "{DO100292}")
    private Long groupId;
    //excel文件
    private byte[] bytes;
    //操作人ID
    private Long operateUserId;
    //操作人名称
    private String operateRealName;
}
}
   