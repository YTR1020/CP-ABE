package com.duwei.cp.abe.parameter;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.ToString;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe
 * @Author: duwei
 * @Date: 2022/7/21 16:27
 * @Description: 系统主私钥
 */
@Data
@ToString
public class MasterPrivateKey extends Key{
    /**
     * beta in Z_p
     */
    private Element beta;
    /**
     * g pow alpha
     */
    private Element g_alpha;
    /**
     * alpha
     */
    private Element alpha;


    private MasterPrivateKey(){

    }

    private MasterPrivateKey(PairingParameter parameter){
        super(parameter);
    }

    public static MasterPrivateKey build(PairingParameter parameter){
        MasterPrivateKey masterPrivateKey = new MasterPrivateKey(parameter);  //根据pairing实例创建系统主私钥
        masterPrivateKey.setBeta(parameter.getZr().newRandomElement().getImmutable());  //从Zr群中随机生成Beta
        masterPrivateKey.setAlpha(parameter.getZr().newRandomElement().getImmutable());  //从Zr群中随机生成Alpha
        masterPrivateKey.setG_alpha((parameter.getGenerator().powZn(masterPrivateKey.getAlpha())).getImmutable());
        //计算g^Alpha
        return masterPrivateKey;
    }


}
