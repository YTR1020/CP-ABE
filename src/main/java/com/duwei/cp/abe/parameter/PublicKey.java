package com.duwei.cp.abe.parameter;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.ToString;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe
 * @Author: duwei
 * @Date: 2022/7/21 16:22
 * @Description: 系统公钥
 */
@Data
@ToString
public class PublicKey extends Key {
    /**
     * h = g ^ beta
     */
    private Element h;
    /**
     * 公开的双线性对 e(g,g)^Alpha
     */
    private Element egg_a;
    /**
     * f = g ^ (1 / beta)
     */
    private Element f;

    private PublicKey() {

    }

    private PublicKey(PairingParameter parameter) {
        super(parameter);
    }


    public static PublicKey build(PairingParameter parameter, MasterPrivateKey msk) {
        PublicKey publicKey = new PublicKey(parameter);  //根据pairing实例创建系统公钥
        publicKey.setH(parameter.getGenerator().powZn(msk.getBeta()).getImmutable());  //根据系统密钥MSK对应参数，计算h=g^Beta
        publicKey.setF(parameter.getGenerator().powZn(msk.getBeta().invert()).getImmutable());  //根据系统密钥MSK对应参数，计算f=g^(1/Beta)
        publicKey.setEgg_a((parameter.getPairing().pairing(parameter.getGenerator(), parameter.getGenerator()).mulZn(msk.getAlpha())).getImmutable());
        //根据系统密钥MSK对应参数，计算e(g,g)^Alpha
        return publicKey;
    }

}
