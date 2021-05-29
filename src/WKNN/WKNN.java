package WKNN;

import dataDTO.RSSI;
import dataDTO.XYaxis;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WKNN {
    /**
     *
     * @param rssi 传入的参数为RSSI类，匹配后得出坐标。
     * @return 返回的是位置信息(x,y)
     */
    public static XYaxis Match_WKNN(RSSI rssi){
        Connection connection = null;//声明Connection对象
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";   //驱动程序名
        String DB_URL = "jdbc:mysql://120.27.237.68/indoor_localization?serverTimezone=Asia/Shanghai";    //URL指向要访问的数据库名 test
        String USER = "root";                     //MySQL配置时的用户名
        String PASSWORD = "Zzz16543213@";                     //MySQL配置时的密码

        //WKNN匹配选用的点的个数
        int WKNN_K = 4;
        //物理空间采集方阵的宽度
        int width = 5;
        //根据给定的WKNN_K的数值，来设定rssi_record RSSI数组的长度，需为K + 1（其实也可以是K，但是后面的dis_record数组的长度时K+1 方便对应）
        //RSSI[]和dis_record数组一一对应，rssi_record数组记录最小WKNN个点，dis_record数组记录对应的最小4个距离
        RSSI[] rssi_record = new RSSI[WKNN_K + 1];
        //初始化每一个数组RSSI元素
        for (int counter = 0;counter < rssi_record.length; counter++){
            rssi_record[counter] = new RSSI();
        }
        //dis_record数组初始化，长度为WKNN_K + 1，下标1——WKNN_K存的距离从大到小排序，届时比较时从1开始，到WKNN_K结束
        //比较时，若距离大于当前的，则取代前面的相对较大的一个，比如WKNN_K = 3，下标1——3对应的距离是9、5、1，此时计算到的距离是2，则取代5。
        //下标0存储多次比较后，比下标1的距离大的数，比如上例，距离是15，在比较下标1时，比当前大，则取代前面的数，也就是取代下标0的。
        double[] dis_record = new double[WKNN_K + 1];
        //需要先将dis_record的数组每个元素设为大数值，以便刚开始比较时，能慢慢从后到前正确填补数组
        Arrays.fill(dis_record, 999999);
        //数据集中每次查询一条数据时，暂时存储数据的变量
        int AP1RSSI; int AP2RSSI; int AP3RSSI;
        int number;
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
                    //MAC地址成功匹配，需要将data_index + 1 计数。
                    data_index = data_index +1;
                    MAC_match = true;
                    AP1RSSI = resultSet.getInt("RSSIAP1");
                    AP2RSSI = resultSet.getInt("RSSIAP2");
                    AP3RSSI = resultSet.getInt("RSSIAP3");
                    number = resultSet.getInt("number");
                    distance = Math.sqrt(Math.pow(rssi.getAP1RSSI() - AP1RSSI, 2)
                            + Math.pow(rssi.getAP2RSSI() - AP2RSSI, 2)
                            + Math.pow(rssi.getAP3RSSI() - AP3RSSI, 2));
                    /*调试代码*/System.out.println("\n点"+number+"到定位点的距离是 "+distance);

                    //在循环中，如果data_index==1，说明在数据库中找到了第一个与定位点MAC地址匹配的点
                    //将第一个点作为最小的点记录。
                    if (data_index == 1) {
                        rssi_record[WKNN_K].setNumber(number);
                        rssi_record[WKNN_K].setFrequency(resultSet.getInt("frequency"));
                        rssi_record[WKNN_K].setAP1RSSI(AP1RSSI);
                        rssi_record[WKNN_K].setAP2RSSI(AP2RSSI);
                        rssi_record[WKNN_K].setAP3RSSI(AP3RSSI);
                        rssi_record[WKNN_K].setAP1MAC(resultSet.getString("MACAP1"));
                        rssi_record[WKNN_K].setAP2MAC(resultSet.getString("MACAP2"));
                        rssi_record[WKNN_K].setAP3MAC(resultSet.getString("MACAP3"));
                        dis_record[WKNN_K] = distance;
                        /*调试代码*/System.out.println("第一个匹配到的点number信息是：\n"+rssi_record[WKNN_K].toString());
                    }
                    //第一次后，在数据库中匹配到MAC对应的点，比较其与记录的WKNN_K个点的距离，如果有比其中小的，则取代！
                    else {
                        //从下标1开始比较，如果大于当前，就取代前面那个相对更大的
                        for (int counter = 1; counter < dis_record.length; counter++) {
                            if (distance > dis_record[counter]){
                                rssi_record[counter -1].setNumber(number);
                                rssi_record[counter -1].setFrequency(resultSet.getInt("frequency"));
                                rssi_record[counter -1].setAP1RSSI(AP1RSSI);
                                rssi_record[counter -1].setAP2RSSI(AP2RSSI);
                                rssi_record[counter -1].setAP3RSSI(AP3RSSI);
                                rssi_record[counter -1].setAP1MAC(resultSet.getString("MACAP1"));
                                rssi_record[counter -1].setAP2MAC(resultSet.getString("MACAP2"));
                                rssi_record[counter -1].setAP3MAC(resultSet.getString("MACAP3"));
                                dis_record[counter - 1] = distance;
                                break;
                            }
                            else {
                                //如果比当前下标存的距离小，且已经循环到最后一个最小的距离，则取代最后一个数，下标length - 1 就是最后一个数——最小的距离。
                                if (counter == dis_record.length - 1){
                                    rssi_record[dis_record.length - 1].setNumber(number);
                                    rssi_record[dis_record.length - 1].setFrequency(resultSet.getInt("frequency"));
                                    rssi_record[dis_record.length - 1].setAP1RSSI(AP1RSSI);
                                    rssi_record[dis_record.length - 1].setAP2RSSI(AP2RSSI);
                                    rssi_record[dis_record.length - 1].setAP3RSSI(AP3RSSI);
                                    rssi_record[dis_record.length - 1].setAP1MAC(resultSet.getString("MACAP1"));
                                    rssi_record[dis_record.length - 1].setAP2MAC(resultSet.getString("MACAP2"));
                                    rssi_record[dis_record.length - 1].setAP3MAC(resultSet.getString("MACAP3"));
                                    dis_record[dis_record.length - 1] = distance;
                                    System.out.println("");
                                    break;
                                }
                            }
                        }
                        /*调试代码*/for (int counter = 1; counter < rssi_record.length; counter++) {
                            System.out.println("此时RSSI内第" + counter + "个点：  " + rssi_record[counter].toString());
                        }
                    }
                }
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
        /*调试代码*/System.out.println("\n共匹配了"+data_index+"个数据\n");
        for (int counter = 1; counter < rssi_record.length; counter++){
            /*调试代码*/System.out.println("距离相对第"+(5-counter)+"近的点是number"+rssi_record[counter].getNumber());
        }
        /*调试代码*/System.out.println();

        //XYaxis[] 数组存储最小WKNN个点的X Y坐标，长度仍然为WKNN_N + 1，此时下标0用来存储最后加和，从1开始计算omega。
        XYaxis[] xYaxis = new XYaxis[WKNN_K + 1];
        //先计算出权值omega的分母，是一个定值。
        double omega_denominator = 0;
        for (int counter = 1; counter < dis_record.length; counter ++){
            omega_denominator += 1 / (dis_record[counter] + 1);
            /*调试代码*/System.out.println("omega分母上第"+counter+"步计算是："+omega_denominator);
        }
        /*调试代码*/System.out.println("omega分母最终结果是："+omega_denominator+"\n");
        //初始化 xYaxis数组
        for (int counter = 0; counter < xYaxis.length; counter ++){
            xYaxis[counter] = new XYaxis();
        }
        //WKNN_N个参考点坐标转换，以及omega的计算
        for (int counter = 1; counter < xYaxis.length; counter ++){
            //采集数据时候，一个格子0.5m，3个格子采集一个数据，一个点区间长度为1.5m
            xYaxis[counter].setX( (rssi_record[counter].getNumber()%width - 1) * 1.5 );
            xYaxis[counter].setY( (double) (rssi_record[counter].getNumber()/width) * 1.5);
            xYaxis[counter].setOmega( ( 1 / ( dis_record[counter] + 1 ) ) / omega_denominator);
            /*调试代码*/System.out.println("第"+counter+"个参考点的信息：\nX："+xYaxis[counter].getX()+"   Y："+xYaxis[counter].getY()+
                    "   omega："+xYaxis[counter].getOmega());
        }
        //计算最终的坐标
        for (int counter = 1; counter < xYaxis.length; counter ++){
            xYaxis[0].setX(xYaxis[0].getX() + xYaxis[counter].getX() * xYaxis[counter].getOmega());
            xYaxis[0].setY(xYaxis[0].getY() + xYaxis[counter].getY() * xYaxis[counter].getOmega());
        }
        XYaxis endXY = new XYaxis();
        endXY.setX(99999);
        endXY.setY(99999);
        /*调试代码*/System.out.println("\n距离最近的点的number为： number"+rssi_record[WKNN_K].getNumber());
        //若能找到MAC匹配的数据则
        if (MAC_match) {
            endXY.setX(xYaxis[0].getX());
            endXY.setY(xYaxis[0].getY());
            return endXY;
        }
        //找不到则报错
        else {
            System.out.println("指纹数据库中未能找到与传入数据有相同MAC地址的数据进行匹配，请检查定位时扫描所选用的WiFi");
            return endXY;
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
//        /*调试代码：*/System.out.println("List的规模是"+rssiArrayList.size());

        //求rssi信号AP1-AP3的RSSI信号强度和,为方便复查，不使用增强型的for
        for (int counter = 0 ; counter < rssiArrayList.size(); counter ++) {
//            /*调试代码：*/System.out.println("RSSI AP1当前是"+rssi.getAP1RSSI()+"  LIST"+(counter+1)+"是"+rssiArrayList.get(counter).getAP1RSSI());
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
//                /*调试代码*/System.out.println("平均值AP1: "+rssi.getAP1RSSI()+"  测试值AP1: "+rssiArrayList.get(counter).getAP1RSSI()+
//                        "  分母上的viAP1: "+Math.pow(rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI(), 2));
            }
            //计算3个AP信号的 3sigma
            sigma3_AP1 = Math.sqrt(viAP1 / (rssiArrayList.size() - 1)) * 3;
            sigma3_AP2 = Math.sqrt(viAP2 / (rssiArrayList.size() - 1)) * 3;
            sigma3_AP3 = Math.sqrt(viAP3 / (rssiArrayList.size() - 1)) * 3;
//            /*调试代码*/System.out.println("sigma3_AP1  "+sigma3_AP1);
            //根据拉依达准则，如果平均值和测试值的误差的绝对值大于3sigma，则为粗大误差，需要删除。
            //比较每个AP信号的RSSI 与 其平均值RSSI的差值，有rssiArrayList.size()组数据，每组的3个AP信号MAC相同。
            for (int counter = 0; counter < rssiArrayList.size(); counter++) {
                if (Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI()) > sigma3_AP1
                        || Math.abs((double) rssi.getAP2RSSI() - rssiArrayList.get(counter).getAP2RSSI()) > sigma3_AP2
                        || Math.abs((double) rssi.getAP3RSSI() - rssiArrayList.get(counter).getAP3RSSI()) > sigma3_AP3) {
                    //删除当前的rssi数据，因为其为粗大误差。
                    rssiArrayList.remove(counter);
//                    /*调试代码*/System.out.println("剔除了数据"+(counter+1)+"\n\n根据拉依达准则再计算：");
                    error_existence = true;
                    //删除该数据后，则需要重新计算每一个rssi，需要退出循环，则令counter值大于List的size，即可退出循环。
                    break;
                }
                else{
//                    /*调试代码*/System.out.println("第"+(counter+1)+"组数据的残差为："+Math.abs((double) rssi.getAP1RSSI() - rssiArrayList.get(counter).getAP1RSSI())+"  未剔除数据");
                }
                if(counter == rssiArrayList.size() - 1){
                    error_existence = false;
                    break;
                }
            }
        } while (error_existence && rssiArrayList.size()!=0);

        return rssiArrayList;
    }

}
