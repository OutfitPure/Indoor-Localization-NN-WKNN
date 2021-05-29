package NN;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.HashMap;
import dataDTO.RSSI;

//进行匹配
public class NN {
    /**
     * 匹配算法
     * @param rssi 从平均算法返回的参数是一个rssi变量
     * @return string型的位置信息，即位置的标号。待定为String的原因是，如果有异常，需要后续修改代码。
     */
    public static String Match(RSSI rssi){
        Connection connection = null;//声明Connection对象
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";   //驱动程序名
        String DB_URL = "jdbc:mysql://120.27.237.68/indoor_localization?serverTimezone=Asia/Shanghai";    //URL指向要访问的数据库名 test
        String USER = "root";                     //MySQL配置时的用户名
        String PASSWORD = "Zzz16543213@";                     //MySQL配置时的密码

        //数据集中每次查询一条数据时，暂时存储数据的变量
        int AP1RSSI; int AP2RSSI; int AP3RSSI;
        int number;
        //标记最小距离的点的位置标号以及最小距离的数值
        int num_index = 0;
        double dis_min = 0;
        //计算当前一条数据与定位点的距离
        double distance;
        //一个变量用来标记收集到的点是否能在数据库中找到MAC地址一一对应的数据来进行匹配。
        boolean MAC_match = false;
        //因为在数据库中的第一个数据有可能MAC不匹配，所以需要设定一个变量来标记数据集中的第一个数据（找出最小值算法需要第一个数据的距离内容）
        int data_index = 0;
        Statement statement = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
//            /*调试代码*/System.out.println("----------------------------------------------------------------------" +
//                    "\nConnecting to database......\n");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);  //1.getConnection()方法，连接MySQL数据库！！
            if (!connection.isClosed())
//                /*调试代码*/System.out.println("Succeeded connecting to the Database!\n");

            // 执行查询
//            /*调试代码*/System.out.println("Instantiate object 'Statement'......\n" +
//                    "----------------------------------------------------------------------\n");
            statement = connection.createStatement();           //2.创建statement类对象，用来执行SQL语句！！
            String sql;                                          //要执行的SQL语句
            sql = "select * from the_data_of_finger_point";
            ResultSet resultSet = statement.executeQuery(sql);     //3.ResultSet类，用来存放获取的结果集！！

            // 展开结果集数据库
            while(resultSet.next()){
                //将指针从当前位置下移一行。ResultSet 指针最初位于第一行之前；
                // 第一次调用 next 方法使第一行成为当前行；
                // 第二次调用使第二行成为当前行，依此类推。 如果新的当前行有效，则返回 true；如果不存在下一行，则返回 false.
                // 通过字段检索

                //先进行rssi与数据集中数据MAC地址的匹配，假设传入的RSSI的MAC地址与数据集中的MAC地址一一对应，则可以进行匹配计算，否则，不进行计算。
                //若均能匹配，则进行后续的计算。不能匹配的数据就不需要再进行一系列的计算了。
                //只需有一条能匹配就可以位置信息。若遍历数据集结束都不能匹配，则说明定位采用的WiFi与数据库中存储的均不匹配。
                if(rssi.getAP1MAC().equals(resultSet.getString("MACAP1"))
                        && rssi.getAP2MAC().equals(resultSet.getString("MACAP2"))
                        && rssi.getAP3MAC().equals(resultSet.getString("MACAP3"))) {
                    MAC_match = true;
                    AP1RSSI = resultSet.getInt("RSSIAP1");
                    AP2RSSI = resultSet.getInt("RSSIAP2");
                    AP3RSSI = resultSet.getInt("RSSIAP3");
                    number = resultSet.getInt("number");
                    distance = Math.sqrt(Math.pow(rssi.getAP1RSSI() - AP1RSSI, 2)
                            + Math.pow(rssi.getAP2RSSI() - AP2RSSI, 2)
                            + Math.pow(rssi.getAP3RSSI() - AP3RSSI, 2));
                    System.out.println("该点到定位点的距离是 "+distance);

                    //进行到这里则说明MAC地址成功匹配了，需要将data_index + 1 计数。
                    data_index = data_index +1;

                    //在循环中，如果data_index==1，说明在数据库中找到了第一个与定位点MAC地址匹配的点，用num_index标记当前最小距离的number，dis_min标记当前最小距离，若num！=1则进行比较操作。
                    if (data_index == 1) {
                        num_index = number;
                        dis_min = distance;
                        /*调试代码*/System.out.println("数据库正在比较的点与当前位置点的距离为" + distance + "  位置标号为" + number +
                                "  历史最小距离为" + dis_min + "  其位置标号为" + num_index);
                    }
                    //如果比记录的距离小，则更新最小距离和最小距离的位置标号，如果不比其小，则不操作。
                    else {
                        if (distance < dis_min) {
                            //如果当前距离小于之前记录的最小距离，则更改
                            num_index = number;
                            dis_min = distance;
                            /*调试代码*/System.out.println("数据库正在比较的点与当前位置点的距离为" + distance + "  位置标号为" + number +
                                    "  历史最小距离为" + dis_min + "  其位置标号为" + num_index);
                        } else {
                            /*调试代码*/System.out.println("数据库正在比较的点与当前位置点的距离为" + distance + "  位置标号为" + number +
                                    "  历史最小距离为" + dis_min + "  其位置标号为" + num_index);
                        }
                    }
                }
                /*调试代码*/System.out.println("共计算了"+data_index+"个数据");
            }
            // 完成后关闭
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(ClassNotFoundException e) {
            //数据库驱动类异常处理

//            /*调试代码*/System.out.println("----------------------------------------------------------------------" +
//                    "\nSorry,can`t find the Driver!\n" +
//                    "----------------------------------------------------------------------\n");
            e.printStackTrace();
        }
        catch(SQLException se){
            // 处理 JDBC 错误
//            /*调试代码*/System.out.println("----------------------------------------------------------------------" +
//                    "\nAn error occurred while processing JDBC\n" +
//                    "----------------------------------------------------------------------\n");
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
//            /*调试代码*/System.out.println("----------------------------------------------------------------------" +
//                    "\nAn error occurred while processing Class.forName\n" +
//                    "----------------------------------------------------------------------\n");
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(statement!=null) statement.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(connection!=null) connection.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        //若能找到MAC匹配的数据则
        if (MAC_match) {
            return String.valueOf(num_index);
        }
        //找不到则报错
        else {
            return "指纹数据库中未能找到与传入数据有相同MAC地址的数据进行匹配，请检查定位时扫描所选用的WiFi";
        }
    }

    /**
     * 平均算法
     * @param rssiArrayList 引入参数为ArrayList，进行平均前的一步是筛选，其rssiArrayList传递到平均算法中
     * @return 返回值为 一个特定的rssi 将其传递到匹配算法中！
     */
    public static RSSI Average(ArrayList<RSSI> rssiArrayList){
        //初始化一个rssi变量，设定其全部属性,定位的时候不需要设定其位置信息number以及频率frequency
        RSSI rssi = new RSSI();
        rssi.setAP1RSSI(0);
        rssi.setAP2RSSI(0);
        rssi.setAP3RSSI(0);
        rssi.setAP1MAC(rssiArrayList.get(0).getAP1MAC());
        rssi.setAP2MAC(rssiArrayList.get(0).getAP2MAC());
        rssi.setAP3MAC(rssiArrayList.get(0).getAP3MAC());
        /*调试代码：*/System.out.println("List的规模是"+rssiArrayList.size());

        //求rssi信号AP1-AP3的RSSI信号强度和,为方便复查，不使用增强型的for
        for (int counter = 0 ; counter < rssiArrayList.size(); counter ++) {
            /*调试代码：*/System.out.println("RSSI AP1当前是"+rssi.getAP1RSSI()+"  LIST"+(counter+1)+"是"+rssiArrayList.get(counter).getAP1RSSI());
            rssi.setAP1RSSI(rssi.getAP1RSSI() + rssiArrayList.get(counter).getAP1RSSI());
            rssi.setAP2RSSI(rssi.getAP2RSSI() + rssiArrayList.get(counter).getAP2RSSI());
            rssi.setAP3RSSI(rssi.getAP3RSSI() + rssiArrayList.get(counter).getAP3RSSI());
        }
        //求平均
        rssi.setAP1RSSI(rssi.getAP1RSSI()/rssiArrayList.size());
        rssi.setAP2RSSI(rssi.getAP2RSSI()/rssiArrayList.size());
        rssi.setAP3RSSI(rssi.getAP3RSSI()/rssiArrayList.size());
        return rssi;
    }

    /**
     * 拉依达准则筛选算法
     * @param rssiArrayList 引入的参数是一个具有四个RSSI数据的ArrayList
     * @return 返回值同样得是ArrayList，因为过程中可能会筛选掉一些数据，返回的是筛选结束后的数据。
     */
    public static ArrayList<RSSI> Screen(ArrayList<RSSI> rssiArrayList){
        //标记当前是否存在有剔除过粗大误差的情况。默认情况为false，即不存在粗大误差。如果计算中出现，则令error_existence = true，展开新的一轮循环
        boolean error_existence = false;
        //循环外定义rssi变量以及viAP1-viAP3——每个信号与平均值的差。
        RSSI rssi = new RSSI();
        double viAP1,viAP2,viAP3;
        double sigma3_AP1,sigma3_AP2,sigma3_AP3;
        do {
            //先求rssiArrayList中AP1-AP3 RSSI的平均值
            rssi.setAP1RSSI(0);
            rssi.setAP2RSSI(0);
            rssi.setAP3RSSI(0);
            rssi.setAP1MAC(rssiArrayList.get(0).getAP1MAC());
            rssi.setAP2MAC(rssiArrayList.get(0).getAP2MAC());
            rssi.setAP3MAC(rssiArrayList.get(0).getAP3MAC());
            //求rssi信号AP1-AP3的RSSI信号强度和,为方便复查，不使用增强型的for
            for (int counter = 0; counter < rssiArrayList.size(); counter++) {
                rssi.setAP1RSSI(rssi.getAP1RSSI() + rssiArrayList.get(counter).getAP1RSSI());
                rssi.setAP2RSSI(rssi.getAP2RSSI() + rssiArrayList.get(counter).getAP2RSSI());
                rssi.setAP3RSSI(rssi.getAP3RSSI() + rssiArrayList.get(counter).getAP3RSSI());
            }
            //求平均
            rssi.setAP1RSSI(rssi.getAP1RSSI() / rssiArrayList.size());
            rssi.setAP2RSSI(rssi.getAP2RSSI() / rssiArrayList.size());
            rssi.setAP2RSSI(rssi.getAP2RSSI() / rssiArrayList.size());
            //计算根号下，分母上的Vi
            viAP1 = 0;
            viAP2 = 0;
            viAP3 = 0;
            for (int counter = 0; counter < rssiArrayList.size(); counter++) {
                viAP1 += Math.pow(rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI(), 2);
                viAP2 += Math.pow(rssi.getAP2RSSI() - rssiArrayList.get(counter).getAP2RSSI(), 2);
                viAP3 += Math.pow(rssi.getAP3RSSI() - rssiArrayList.get(counter).getAP3RSSI(), 2);
                /*调试代码*/System.out.println("平均值AP1: "+rssi.getAP1RSSI()+"  测试值AP1: "+rssiArrayList.get(counter).getAP1RSSI()+
                        "  分母上的viAP1: "+Math.pow(rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI(), 2));
            }
            //计算3个AP信号的 3sigma
            sigma3_AP1 = Math.sqrt(viAP1 / (rssiArrayList.size() - 1)) * 3;
            sigma3_AP2 = Math.sqrt(viAP2 / (rssiArrayList.size() - 1)) * 3;
            sigma3_AP3 = Math.sqrt(viAP3 / (rssiArrayList.size() - 1)) * 3;
            /*调试代码*/System.out.println("sigma3_AP1  "+sigma3_AP1+"\nsigma3_AP2  "+sigma3_AP2+"\nsigma3_AP3  "+sigma3_AP3);
            //根据拉依达准则，如果平均值和测试值的误差的绝对值大于3sigma，则为粗大误差，需要删除。
            //比较每个AP信号的RSSI 与 其平均值RSSI的差值，有rssiArrayList.size()组数据，每组的3个AP信号MAC相同。
            for (int counter = 0; counter < rssiArrayList.size(); counter++) {
                if (Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI()) > sigma3_AP1
                        || Math.abs((double) rssi.getAP2RSSI() - rssiArrayList.get(counter).getAP2RSSI()) > sigma3_AP2
                        || Math.abs((double) rssi.getAP3RSSI() - rssiArrayList.get(counter).getAP3RSSI()) > sigma3_AP3) {
                    //删除当前的rssi数据，因为其为粗大误差。
                    rssiArrayList.remove(counter);
                    /*调试代码*/System.out.println("第"+(counter+1)+"组数据的三个残差分别为："
                            +Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI())+"  "
                            +Math.abs((double) rssi.getAP2RSSI() - rssiArrayList.get(counter).getAP2RSSI())+"  "
                            +Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI())+"  "
                            +"  剔除了数据"+(counter+1)+"\n\n根据拉依达准则再计算：");
                    error_existence = true;
                    //删除该数据后，则需要重新计算每一个rssi，需要退出循环，则令counter值大于List的size，即可退出循环。
                    break;
                }
                else{
                    /*调试代码*/System.out.println("第"+(counter+1)+"组数据的三个残差分别为："
                            +Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI())+"  "
                            +Math.abs((double) rssi.getAP2RSSI() - rssiArrayList.get(counter).getAP2RSSI())+"  "
                            +Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI())+"  "
                            +"  未剔除数据");
                }
                if(counter == rssiArrayList.size() - 1){
                    error_existence = false;
                    break;
                }
            }
        } while (error_existence && rssiArrayList.size()!=0);

        return rssiArrayList;
    }


    public static void main(String[] args) {

    }
}
