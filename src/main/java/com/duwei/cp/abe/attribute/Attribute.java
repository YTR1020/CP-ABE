package com.duwei.cp.abe.attribute;

import com.duwei.cp.abe.parameter.PublicKey;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.Data;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.attribute
 * @Author: duwei
 * @Date: 2022/7/22 9:04
 * @Description: 用户的属性描述
 * 属性空间 G0
 */
@Data
public class Attribute {
    /**
     * 用Element元素来标识用户属性
     */
    private Element attributeValue;  //用户属性的Element元素
    private String attributeName;  //用户属性的字符描述

    public Attribute(String attributeName, PublicKey publicKey){
        this(attributeName,publicKey.getPairingParameter().getG0());  //设置该用户属性对应的系统公钥参数//从公钥参数中获取生成该叶子节点对应的Element属性值
    }

    public Attribute(String attributeName, Field G0){
        this.attributeName = attributeName;
        this.attributeValue = G0.newElementFromBytes(attributeName.getBytes(StandardCharsets.UTF_8)).getImmutable();
        //将属性字符串转换为字节数组，并使用G0群的Element元素来表示
    }

    @Override
    public String toString(){
        return attributeName;
    }

    //判断两个类的参数是否相等
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Attribute)) {
            return false;
        }
        Attribute attribute1 = (Attribute) o;
        return Objects.equals(attributeValue, attribute1.attributeValue) && Objects.equals(attributeName, attribute1.attributeName);
    }

    //获取对应哈希码
    @Override
    public int hashCode() {
        return Objects.hash(attributeValue, attributeName);
    }
}
