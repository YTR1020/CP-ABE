package com.duwei.cp.abe.engine;

import com.duwei.cp.abe.attribute.Attribute;
import com.duwei.cp.abe.parameter.*;
import com.duwei.cp.abe.polynomial.Polynomial;
import com.duwei.cp.abe.structure.*;
import com.duwei.cp.abe.text.CipherText;
import com.duwei.cp.abe.text.PlainText;
import com.duwei.cp.abe.util.ConvertUtils;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.util.*;

/**
 * @BelongsProject: JPBC-ABE
 * @BelongsPackage: com.duwei.jpbc.cp.abe
 * @Author: duwei
 * @Date: 2022/7/21 16:30
 * @Description: 算法引擎
 */
public class CpAneEngine {

    //生成密钥：用户私钥SK={D=g^[(Alpha+r)/Beta],Dj=(g^r)*[H(j)^rj],Dj'=g^rj,j∈S}
    public UserPrivateKey keyGen(MasterPrivateKey masterPrivateKey, List<Attribute> attributes) {
        return UserPrivateKey.build(masterPrivateKey, attributes);
    }


    /**
     * 基于公共参数和访问树结构加密消息
     *
     * @param pk
     * @param plainText
     * @param accessTree
     * @return
     */

    //输入：系统公钥PK={h=g^Beta,f=g^(1/Beta),e(g,g)^Alpha,g,G0，G1,Zp}，明文M，访问控制树。
    //输出：密文CT={ T, C~ = M * e(g,g)^(Alpha*s), C = h^s, Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0) }
    public CipherText encrypt(PublicKey pk, PlainText plainText, AccessTree accessTree) {
        AccessTreeNode root = accessTree.getRoot();

        //根节点的秘密数
        Element s = getRandomElementInZr(pk);  //从Zr加法循环群中依据公钥参数随机生成一个s
        root.setSecretNumber(s);  //令根节点的秘密数为qr(0)=s

        CipherText cipherText = new CipherText();  //新建密文类

        //1.设置密文第一部分

        Element c_ware = (plainText.getMessageValue().mul(pk.getEgg_a().powZn(s).getImmutable())).getImmutable();
        //C~ = M * e(g,g)^(Alpha*s) 【从公钥里获取e(g,g)^Alpha】
        cipherText.setC_wave(c_ware);

        //2.设置密文第二部分
        Element c = pk.getH().powZn(s).getImmutable();
        //C = h^s 【从公钥里获取 h=g^beta，也就是说 C = g^(Beta*s)】
        cipherText.setC(c);

        //3.递归设置子节点的秘密值qx(y)，Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0)
        compute(root, pk, cipherText);

        //设置访问树T
        cipherText.setAccessTree(accessTree);
        return cipherText;
    }

    /**
     * 在Z_r上选取随机元素
     *
     * @param publicKey
     * @return
     */
    private Element getRandomElementInZr(PublicKey publicKey) {
        return publicKey.getPairingParameter().getZr().newRandomElement().getImmutable();
    }

    //递归设置子节点的秘密值，Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0)
    private void compute(AccessTreeNode node, PublicKey publicKey, CipherText cipherText) {
        Field z_r = publicKey.getPairingParameter().getZr();  //获取Zr加法循环群
        Element secretNumber = node.getSecretNumber();  //获取节点秘密值
        int childrenSize = node.getChildrenSize();  //获取孩子节点个数

        //如果为内部节点
        if (node.getAccessTreeNodeType() == AccessTreeNodeType.INNER_NODE) {
            //内部节点选择的多项式，阶为kx-1，s=秘密值，加法循环群里计算
            Polynomial polynomial = new Polynomial(((InnerAccessTreeNode)node).getThreshold() - 1, secretNumber, z_r);
            //遍历该内部节点的所有孩子节点，
            for (AccessTreeNode child : node.getChildren()) {
                int index = child.getIndex();  //获取孩子节点的索引值
                Element childSecret = polynomial.getValue(z_r.newElement(index).getImmutable());  //依据索引值i，计算该孩子节点对应qx(i)
                child.setParent(node);  //设置父节点
                child.setSecretNumber(childSecret);  //设置孩子节点的秘密值为qx(i)
                //递归去设置子节点
                compute(child, publicKey, cipherText);
            }
        }

        //节点是叶节点
        if (node.getAccessTreeNodeType() == AccessTreeNodeType.LEAF_NODE) {
            LeafAccessTreeNode leafNode = (LeafAccessTreeNode) node;

            //属性
            Attribute attribute = leafNode.getAttribute();
            //属性值
            Element attributeValue = attribute.getAttributeValue();

            Element c_y = (publicKey.getPairingParameter().getGenerator().powZn(leafNode.getSecretNumber())).getImmutable();
            //Cy = g^qy(0) = g^qi(0)

            Element c_y_pie = (publicKey.hash(
                    attributeValue.powZn(leafNode.getSecretNumber())
            ).getImmutable());
            //Cy' = [H(att(y))]^qy(0)

            cipherText.putCy(attribute, c_y);
            cipherText.putCyPie(attribute, c_y_pie);
        }
    }


    //Element转比特数组去掉前后的0转字符串
    public String decryptToStr(PublicKey publicKey, UserPrivateKey userPrivateKey, CipherText cipherText){
        Element decrypt = decrypt(publicKey, userPrivateKey, cipherText);
        if (decrypt != null){
            return new String(ConvertUtils.byteToStr(decrypt.toBytes()));
        }
        return null;
    }

    //解密最终计算
    //输入1：系统公钥PK={h=g^Beta,f=g^(1/Beta),e(g,g)^Alpha,g,G0，G1,Zp}
    //输入2：用户私钥SK={D=g^[(Alpha+r)/Beta],Dj=(g^r)*[H(j)^rj],Dj'=g^rj,j∈S}
    //输入3：密文CT={ T, C~ = M * e(g,g)^(Alpha*s), C = h^s, Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0) }
    //拉格朗日差值算法或直接计算，递归恢复每个解密节点的秘密值，获得 A = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
    //输出：M (Element类)
    public Element decrypt(PublicKey publicKey, UserPrivateKey userPrivateKey, CipherText cipherText) {
        //拉格朗日差值算法或直接计算，递归恢复每个解密节点的秘密值，获得 A = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
        Element decryptNode = decryptNode(publicKey, userPrivateKey, cipherText, cipherText.getAccessTree().getRoot(), userPrivateKey.getUserAttributes());
        if (decryptNode != null) {
            Element D = userPrivateKey.getD();  //从用户私钥中获取D=g^[(Alpha+r)/Beta]
            Element C = cipherText.getC();  //从密文中获取C = h^s
            Element c_wave = cipherText.getC_wave();  //从密文中获取C~ = M * e(g,g)^(Alpha*s)
            Pairing pairing = publicKey.getPairingParameter().getPairing();  //从公钥中获取Pairing实例各参数
            return c_wave.div(pairing.pairing(C, D).div(decryptNode));  //计算 M = C~/[e(C,D)/A] = M * e(g,g)^(Alpha*s) / e(g,g)^(Alpha*s)
        }
        return null;
    }


    //解密到G1上
    //输出：拉格朗日差值算法或直接计算，递归恢复每个解密节点的秘密值，获得 A = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
    //输入1：系统公钥PK={h=g^Beta,f=g^(1/Beta),e(g,g)^Alpha,g,G0，G1,Zp}
    //输入2：用户私钥SK={D=g^[(Alpha+r)/Beta],Dj=(g^r)*[H(j)^rj],Dj'=g^rj,j∈S}
    //输入3：密文CT={ T, C~ = M * e(g,g)^(Alpha*s), C = h^s, Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0) }
    //输入4：需要恢复秘密值的访问控制树T的节点x
    //输入5：属性集
    public Element decryptNode(PublicKey publicKey, UserPrivateKey userPrivateKey, CipherText cipherText, AccessTreeNode x, List<Attribute> attributes) {
        //如果是叶子节点
        if (x.getAccessTreeNodeType() == AccessTreeNodeType.LEAF_NODE) {
            LeafAccessTreeNode leafNode = ((LeafAccessTreeNode) x);
            Attribute attribute = leafNode.getAttribute();  //获取叶子节点的属性
            if (attributes.contains(attribute)) {  //如果属性满足叶子节点属性子集
                Element cy = cipherText.getCy(attribute);  //从密文中获取Cy = g^qy(0) = g^qi(0)
                Element cyPie = cipherText.getCyPie(attribute);  //从密文中获取Cy' = [H(att(y))]^qy(0)
                Element dj = userPrivateKey.getDj(attribute);  //从用户私钥中获取Dj=(g^r)*[H(j)^rj]
                Element djPie = userPrivateKey.getDjPie(attribute);  //从用户私钥中获取Dj'=g^rj
                Pairing pairing = userPrivateKey.getPairingParameter().getPairing();  //从用户私钥中获取Pairing实例
                return pairing.pairing(dj, cy).div(pairing.pairing(djPie, cyPie)).getImmutable();
                //计算 A = e(Dj,Cy)/e(Dj',Cy') = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
            } else {
                return null;
            }
        } else {
            //如果是内部节点
            InnerAccessTreeNode innerNode = ((InnerAccessTreeNode) x);
            int threshold = innerNode.getThreshold();  //获取阈值kx
            int satisfyCount = 0;  ////统计可解密的节点个数

            //重建
            Map<Element, Element> indexFzMap = new HashMap<>();
            //遍历孩子节点索引重建，以方便递归解密
            for (AccessTreeNode child : innerNode.getChildren()) {
                Element decryptNode = decryptNode(publicKey, userPrivateKey, cipherText, child, attributes);
                if (decryptNode != null) {
                    satisfyCount++;
                    Element index = publicKey.getPairingParameter().getZr().newElement(child.getIndex()).getImmutable();
                    indexFzMap.put(index, decryptNode);
                }
            }
            if (satisfyCount < threshold) {
                //如果可解密的节点小于阈值，则不可解密，返回空
                return null;
            }

            //初始化
            Element result = publicKey.getPairingParameter().getG1().newOneElement();
            Element zero = publicKey.getPairingParameter().getZr().newZeroElement().getImmutable();
            List<Element> Sx = new ArrayList<>(indexFzMap.keySet());

            //遍历，递归计算解密
            for (Map.Entry<Element, Element> entry : indexFzMap.entrySet()) {
                Element curIndex = entry.getKey();  //获取索引值
                Element curFz = entry.getValue();  //获取Fz=e(Di,Cy)
                Element powZn = Polynomial.lagrangeCoefficient(curIndex, Sx, zero, publicKey.getPairingParameter().getZr());
                //计算对应拉格朗日因子δ(0)，作为Fz=e(Di,Cy)的指数。目标值x为0，即qy(0)的拉格朗日因子。
                result.mul((curFz.powZn(powZn)));
                //连乘计算 A = 连乘Fz^δi(0) = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
            }
            return result.getImmutable();
        }
    }
}
