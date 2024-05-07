package com.duwei.cp.abe.parameter;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import lombok.Data;
import lombok.ToString;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe.parameter
 * @Author: duwei
 * @Date: 2022/7/22 15:06
 * @Description: 双线性对参数
 */
@Data
@ToString
public class PairingParameter {
    private Pairing pairing;
    private Field G0;
    private Field G1;
    private Field Zr;
    private Element generator;

    private PairingParameter() {

    }


    public static PairingParameter getInstance() {
        PairingParameter pairingParameter = new PairingParameter();
        Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
        //从文件导入椭圆曲线参数，生成Pairing实例

        pairingParameter.setPairing(pairing);  //基于基本参数，设置pairing属性
        pairingParameter.setG0(pairing.getG1());  //基于基本参数，设置G0为JPBC库中的G1乘法循环群
        pairingParameter.setG1(pairing.getGT());  //基于基本参数，设置G1为JPBC库中的GT双线性映射乘法循环群
        pairingParameter.setZr(pairing.getZr());  //基于基本参数，设置Zr为JPBC库中的Zr加法循环群
        pairingParameter.setGenerator(pairingParameter.getG0().newRandomElement().getImmutable());
        //基于基本参数，从G0乘法循环群中随机选取Element整数生成元g（作为阶数）

        return pairingParameter;
    }


}
