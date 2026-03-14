package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //计算出所有的日期
        List<LocalDate> dateList = getDateList(begin, end);
    
        //计算所有日期的营业额
        List<Double> turnoverList = getTurnoverByDateList(dateList);
    
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        //将日期列表转换成 String 以逗号分隔
        turnoverReportVO.setDateList(StringUtils.join(dateList,","));
        //将营业额列表转换成 String 以逗号分隔
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //计算用户数
        List<Integer> newUserCountList = new ArrayList<>();
        List<Integer> totalUserCountList = new ArrayList<>();
            
        for(LocalDate date : dateList){
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            Map<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer newUserCount = userMapper.countByMap(map);
            log.info("用户数统计，日期：{}，新增用户数：{}", date, newUserCount);
            newUserCount = newUserCount == null ? 0 : newUserCount;
            newUserCountList.add(newUserCount);
                
            //计算累计用户总数（从开始到该日期结束时的总用户数）
            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("endTime", endTime);
            Integer totalUserCount = userMapper.countByMap(totalMap);
            totalUserCount = totalUserCount == null ? 0 : totalUserCount;
            totalUserCountList.add(totalUserCount);
        }
            
        //将用户数列表转换成 String 以逗号分隔
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserCountList,","));
        userReportVO.setNewUserList(StringUtils.join(newUserCountList,","));
        return userReportVO;
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        // 调用 getBusinessData 获取营业数据
        BusinessDataVO businessData = getBusinessData(begin, end);
        
        List<LocalDate> dateList = getDateList(begin, end);
        
        // 使用 Stream 收集每日订单数据
        List<Integer> orderCountList = dateList.stream()
                .map(date -> getOrderCountByDate(date, null))
                .collect(Collectors.toList());
        
        List<Integer> validOrderList = dateList.stream()
                .map(date -> getOrderCountByDate(date, Orders.COMPLETED))
                .collect(Collectors.toList());
        
        // 构建返回对象
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderList, ","))
                .totalOrderCount(businessData.getValidOrderCount())
                .validOrderCount(businessData.getValidOrderCount())
                .orderCompletionRate(businessData.getOrderCompletionRate())
                .build();
    }

    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        // 获取时间范围
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
            
        log.info("查询销量 top10，时间范围：{} ~ {}", beginTime, endTime);
            
        // 先检查一下有多少已完成的订单
        Map<String, Object> map = new HashMap<>();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        Integer completedOrderCount = orderMapper.countByMap(map);
        log.info("已完成订单数量：{}", completedOrderCount);
            
        // 查询销量前 10 的菜品
        List<GoodsSalesDTO> salesList = orderDetailMapper.getSalesTop10(beginTime, endTime);
            
        log.info("查询结果数量：{}", salesList.size());
        if (!salesList.isEmpty()) {
            salesList.forEach(dto -> log.info("菜品：{},销量：{}", dto.getName(), dto.getNumber()));
        } else {
            log.warn("没有查询到销量数据，请检查:");
            log.warn("1. order_detail 表是否有数据");
            log.warn("2. 已完成的订单是否有关联的订单明细");
        }
            
        // 提取菜名和销量列表
        List<String> nameList = salesList.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());
            
        List<Integer> numberList = salesList.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());
            
        log.info("nameList: {}", nameList);
        log.info("numberList: {}", numberList);
            
        // 构建返回对象
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        log.info("获取营业数据，时间范围：{} ~ {}", begin, end);
        
        // 获取日期列表
        List<LocalDate> dateList = getDateList(begin, end);
        
        // 统计营业额（所有完成订单的总营业额）
        List<Double> turnoverList = getTurnoverByDateList(dateList);
        Double totalTurnover = turnoverList.stream().mapToDouble(Double::doubleValue).sum();
        
        // 统计订单数量
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        for (LocalDate date : dateList) {
            totalOrderCount += getOrderCountByDate(date, null);
            validOrderCount += getOrderCountByDate(date, Orders.COMPLETED);
        }
        
        // 计算订单完成率（0-1 之间）
        Double orderCompletionRate = totalOrderCount == 0 ? 0 : validOrderCount.doubleValue() / totalOrderCount;
        orderCompletionRate = Math.round(orderCompletionRate * 100.0) / 100.0;
        
        // 计算平均客单价
        Double unitPrice = validOrderCount == 0 ? 0 : totalTurnover / validOrderCount;
        unitPrice = Math.round(unitPrice * 100.0) / 100.0;
        
        // 统计新增用户数（指定时间范围内的新增用户总数）
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("beginTime", beginTime);
        userMap.put("endTime", endTime);
        Integer newUsers = userMapper.countByMap(userMap);
        newUsers = newUsers == null ? 0 : newUsers;
        
        // 构建返回对象
        return BusinessDataVO.builder()
                .turnover(Math.round(totalTurnover * 100.0) / 100.0)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //获取近30天时间范围
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().plusDays(1);
        log.info("导出营业数据，时间范围：{} ~ {}", begin, end);
        //获取营业数据
        BusinessDataVO businessData = getBusinessData(begin, end);
        // 这里可以使用 Apache POI 或其他库来生成 Excel 文件并写入 response 输出流
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            //基于模板创建一个新的excel文件
            XSSFWorkbook excel = null;
            if (in != null) {
                excel = new XSSFWorkbook(in);
            }
            //获取Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //将数据写入到指定单元格
            sheet.getRow(1).getCell(1).setCellValue("时间范围：" + begin + " ~ " + end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //插入明细数据
            for(int i = 0; i < 30; i++){
                BusinessDataVO businessDataVO = getBusinessData(begin.plusDays(i), begin.plusDays(i+1));
                row = sheet.createRow(7 + i);
                row.createCell(1).setCellValue(begin.plusDays(i).toString());
                row.createCell(2).setCellValue(businessDataVO.getTurnover());
                row.createCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row.createCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.createCell(5).setCellValue(businessDataVO.getUnitPrice());
                row.createCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //通过输出流将excel文件写出到客户端
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        }catch(Exception e){
            log.error("导出营业数据失败", e);
        }

    }


    /**
     * 根据日期和订单状态统计订单数量
     * @param date 日期
     * @param status 订单状态，为 null 时统计所有状态
     * @return 订单数量
     */
    private Integer getOrderCountByDate(LocalDate date, Integer status) {
        LocalDateTime beginTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        if (status != null) {
            map.put("status", status);
        }
        
        Integer count = orderMapper.countByMap(map);
        count = count == null ? 0 : count;
        
        log.info("订单数统计，日期：{}，状态：{}，订单数：{}", date, status == null ? "全部" : status, count);
        return count;
    }

    /**
     * 根据日期列表统计营业额
     * @param dateList 日期列表
     * @return 营业额列表
     */
    private List<Double> getTurnoverByDateList(List<LocalDate> dateList) {
        List<Double> turnoverList = new ArrayList<>();
        
        for(LocalDate date : dateList){
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            Map<String, Object> map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            log.info("营业额统计，日期：{}，营业额：{}", date, turnover);
            turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
        }
        
        return turnoverList;
    }

    /**
     * 获取日期列表
     * @param begin 开始日期
     * @param end 结束日期
     * @return 日期列表
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(begin.isBefore(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
