package com.duwei.cp.abe.polynomial;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.Data;

import java.util.Arrays;
import java.util.List;


/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.polynomial
 * @Author: duwei
 * @Date: 2022/7/22 9:43
 * @Description: 多项式表示
 */
@Data
public class Polynomial {
    /**
     * 多项式阶数dx=kx-1
     */
    private int degree;
    /**
     * 系数，从低位到高位
     */
    private Element[] coefficients;

    /**
     * 在整数环R上运算，即整数加法循环群Zp
     */
    private Field z_r;

    public Polynomial(int degree,Element s0,Field z_r) {
        this.degree = degree;  //多项式阶数dx=kx-1
        this.z_r = z_r;  //在整数环R上运算，即整数加法循环群Zp
        coefficients = new Element[degree + 1];  //共kx项对应的系数集合
        coefficients[0] = s0;  //首项系数为s0，即qx(0)=s0
        for (int i = 1; i <= degree; i++) {
            coefficients[i] = z_r.newRandomElement().getImmutable();  //多项式其他dx个系数随机选取来完全定义qx
        }
    }

    public Polynomial(int degree, Element[] coefficients,Field z_r) {
        this.degree = degree;  //多项式阶数dx=kx-1
        this.z_r = z_r;  //在整数环R上运算，即整数加法循环群Zp
        this.coefficients = coefficients;  //共kx项对应的系数集合
    }

    /**
     * 获取多项式代入x的值
     * @param x
     * @return
     */
    public Element getValue(Element x){
        //初始化为0
        Element result = z_r.newZeroElement();
        Element temp = z_r.newOneElement();
        //遍历计算多项式的相加结果q(x)=a[0]+a[1]x+a[2]x^2+...+a[n-1]x^(n-1)
        for (Element coefficient : coefficients){
            result.add(coefficient.mul(temp));
            temp.mul(x);
        }
        return result.getImmutable();
    }


    //计算拉格朗日因子lagrange(i, S, x, Zr)→δi
    //拉格朗日因子计算 i是集合S中的某个元素，x是目标点的x值
    public static Element lagrangeCoefficient(Element i, List<Element> s,Element x,Field zr){
        Element result = zr.newOneElement();
        //遍历S集合
        for (Element element : s) {
            if (!i.equals(element)){
                result.mul(x.sub(element).div(i.sub(element)));  //(x-xj)/(xi-xj)的乘积（i≠j)
            }
        }
        return result.getImmutable();
    }

}
