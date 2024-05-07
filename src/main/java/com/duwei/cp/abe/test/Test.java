package com.duwei.cp.abe.test;

import com.duwei.cp.abe.attribute.Attribute;
import com.duwei.cp.abe.engine.CpAneEngine;
import com.duwei.cp.abe.parameter.*;
import com.duwei.cp.abe.structure.AccessTree;
import com.duwei.cp.abe.structure.AccessTreeBuildModel;
import com.duwei.cp.abe.structure.AccessTreeNode;
import com.duwei.cp.abe.text.CipherText;
import com.duwei.cp.abe.text.PlainText;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @BelongsProject: CP-ABE
 * @BelongsPackage: com.duwei.cp.abe.text
 * @Author: duwei
 * @Date: 2022/7/25 16:32
 * @Description: 测试类
 */
public class Test {


    public static void test1() {
        //1.生成系统密钥  包含公钥-私钥
        SystemKey systemKey = SystemKey.build();
        //系统主私钥MK={beta,alpha,g^Alpha}
        //系统公钥PK={h=g^Beta,f=g^(1/Beta),e(g,g)^Alpha,g,G0，G1,Zp}

        //2.设置用户属性
        List<Attribute> attributes = Arrays.asList(
               // new Attribute("学生", systemKey.getPublicKey()),
                //new Attribute("老师", systemKey.getPublicKey()),
                new Attribute("硕士", systemKey.getPublicKey())//,  //新建用户属性“硕士”及对应的Element属性元素
                //new Attribute("护士", systemKey.getPublicKey())
               // new Attribute("二班", systemKey.getPublicKey())
        );

        //新建主算法引擎
        CpAneEngine cpAneEngine = new CpAneEngine();

        //3.生成用户私钥
        UserPrivateKey userPrivateKey = cpAneEngine.keyGen(systemKey.getMasterPrivateKey(), attributes);
        //用户私钥SK={D=g^[(Alpha+r)/Beta],Dj=(g^r)*[H(j)^rj],Dj'=g^rj,j∈S}

        //4.明文
        String plainTextStr = "你好，CP - ABE，我是3201603102";
        PlainText plainText = new PlainText(plainTextStr, systemKey.getPublicKey());  //将明文字符串转换为字节数组，并使用G1群的Element元素来表示
        System.out.println("plainTextStr : " + plainTextStr);

        //5.构建访问控制树
        AccessTree accessTree = getAccessTree(systemKey.getPublicKey());

        //6.加密
        CipherText cipherText = cpAneEngine.encrypt(systemKey.getPublicKey(), plainText, accessTree);
        //密文CT={ T, C~ = M * e(g,g)^(Alpha*s), C = h^s, Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0) }
        System.out.println("cipherText : " + cipherText);

        //7.解密
        String decryptStr = cpAneEngine.decryptToStr(systemKey.getPublicKey(), userPrivateKey, cipherText);
        //递归解密
        //解密最终计算
        //输入1：系统公钥PK={h=g^Beta,f=g^(1/Beta),e(g,g)^Alpha,g,G0，G1,Zp}
        //输入2：用户私钥SK={D=g^[(Alpha+r)/Beta],Dj=(g^r)*[H(j)^rj],Dj'=g^rj,j∈S}
        //输入3：密文CT={ T, C~ = M * e(g,g)^(Alpha*s), C = h^s, Cy = g^qy(0) = g^qi(0)，Cy' = [H(att(y))]^qy(0) }
        //拉格朗日差值算法或直接计算，递归恢复每个解密节点的秘密值，获得 A = e(g,g)^(r*qx(0)) = e(g,g)^(r*s)
        //计算 M = C~/[e(C,D)/A] = M * e(g,g)^(Alpha*s) / e(g,g)^(Alpha*s)
        //输出：M
        System.out.println("decryptStr : " + decryptStr);
    }


    public static void main(String[] args) {
        test1();
    }


    public static AccessTree getAccessTree(PublicKey publicKey) {
        AccessTreeBuildModel[] accessTreeBuildModels = new AccessTreeBuildModel[7];  //访问控制树大小为7
        //根节点ID必须为1
        accessTreeBuildModels[0] = AccessTreeBuildModel.innerAccessTreeBuildModel(1, 2, 1, -1);  //根节点ID为1，阈值为2，索引值为1，无父节点
        accessTreeBuildModels[1] = AccessTreeBuildModel.leafAccessTreeBuildModel(2, 1, "学生", 1);  //ID为2的叶子节点，索引值为1，属性为“学生”，父节点ID为1（根节点）
        accessTreeBuildModels[2] = AccessTreeBuildModel.leafAccessTreeBuildModel(3, 2, "老师", 1);  //ID为3的叶子节点，索引值为2，属性为“老师”，父节点ID为1（根节点）
        accessTreeBuildModels[3] = AccessTreeBuildModel.leafAccessTreeBuildModel(4, 3, "硕士", 1);  //ID为4的叶子节点，索引值为3，属性为“硕士”，父节点ID为1（根节点）
        accessTreeBuildModels[4] = AccessTreeBuildModel.innerAccessTreeBuildModel(5, 1, 4, 1);    //ID为5的内部节点，阈值为1，索引值为4，父节点ID为1（根节点）
        accessTreeBuildModels[5] = AccessTreeBuildModel.leafAccessTreeBuildModel(6, 1, "二班", 5);  //ID为6的叶子节点，索引值为1，属性为“二班”，父节点ID为5（根节点的第4个孩子节点）
        accessTreeBuildModels[6] = AccessTreeBuildModel.leafAccessTreeBuildModel(7, 2, "护士", 5);  //ID为7的叶子节点，索引值为2，属性为“护士”，父节点ID为5（根节点的第4个孩子节点）
        return AccessTree.build(publicKey, accessTreeBuildModels);
    }

    //获取Pairing基本参数
    public static Pairing getPairing() {
        return PairingFactory.getPairing("params/curves/a.properties");
    }

    //递归获取父节点
    public static void pre(AccessTreeNode node) {
        System.out.println(node);
        for (AccessTreeNode child : node.getChildren()) {
            pre(child);
        }
    }

}
