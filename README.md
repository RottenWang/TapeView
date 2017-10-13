# TapeView


效果图

![image](effect.gif)

### 关于注释
   代码是前两天刚好项目有需求写的  所以变量的定义以及一些注释 都是和金额有关的可能看起来不是很直观
   因为项目并没有要求精确到0.1  所以只是精确到了1  为了精确到0.1  把所有传入的值 在绘制以及向外传递值的时候 都被 除以了10

### 主要方法
1.设置不同的数值
setNumbers(int delta, int min, int max, int start, int ranges, int defalutSelected)
参数注释 delta :刻度值   实际刻度为传入的刻度/10
        min: 可选择到的最小值 实际数值位传入值/10
        max: 可选择到的最大值  实际数值位传入值/10
        start: 绘制的刻度的最小值  实际位传入值/10
        ranges: 绘制的范围 实际范围位传入值/10  一般传0即可
        defalutSelected :默认的值  实际位传入值/10
2.设置绘制的一些颜色
setColors(int devideAndMoneyColor, int moneyColor, int midLineColor, int titleColor)
devideAndMoneyColor 刻度和刻度金额颜色
moneyColor          当前金额的颜色
midLineColor        中线颜色
titleColor          标题颜色
