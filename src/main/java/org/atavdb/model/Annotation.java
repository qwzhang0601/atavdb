package org.atavdb.model;

import org.atavdb.service.model.EffectManager;
import org.atavdb.global.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.atavdb.service.util.FormatManager;
import org.atavdb.service.util.MathManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author nick
 */
@Component
@Scope("prototype")
@ComponentScan("org.atavdb.service.model")
public class Annotation {
    
    @Autowired
    EffectManager effectManager;
    
    public String effect;
    public int effectID;
    public String geneName;
    public int stableId;
    public String HGVS_c;
    public String HGVS_p;
    public float polyphenHumdiv = Data.FLOAT_NA;
    public float polyphenHumvar = Data.FLOAT_NA;
    private boolean isValid;
    
    public static final int TRANSCRIPT_LENGTH = 15;      
    
    public void init(ResultSet rset) throws SQLException {        
        stableId = rset.getInt("transcript_stable_id");

        if (stableId < 0) {
            stableId = Data.INTEGER_NA;
        }

        effectID = rset.getInt("effect_id");
        effect = effectManager.getEffectById(effectID).replace("_variant", "");
        HGVS_c = FormatManager.getString(rset.getString("HGVS_c"));
        HGVS_p = FormatManager.getString(rset.getString("HGVS_p"));
        geneName = FormatManager.getString(rset.getString("gene"));

        polyphenHumdiv = MathManager.devide(FormatManager.getInt(rset, "polyphen_humdiv"), 1000);
        polyphenHumvar = MathManager.devide(FormatManager.getInt(rset, "polyphen_humvar"), 1000);
    }

    public void setValid(boolean value) {
        isValid = value;
    }
    
    public boolean isValid() {
        return isValid;
    }

    public String getEffect() {
        return effect;
    }

    public String getHGVS_c() {
        return HGVS_c;
    }

    public String getHGVS_p() {
        return HGVS_p;
    }

    public String getGeneName() {
        return geneName;
    }
    
    public String getStableId() {
        if (stableId == Data.INTEGER_NA) {
            return Data.STRING_NA;
        }

        StringBuilder idSB = new StringBuilder(String.valueOf(stableId));

        int zeroStringLength = TRANSCRIPT_LENGTH - idSB.length() - 4;

        for (int i = 0; i < zeroStringLength; i++) {
            idSB.insert(0, 0);
        }

        idSB.insert(0, "ENST");

        return idSB.toString();
    }
    
    public String getPolyphen() {
        return getPrediction(polyphenHumdiv, effect);
    }
    
    private String getPrediction(float score, String effect) {
        if (score == Data.FLOAT_NA) {
            if (effect.startsWith("missense_variant")
                    || effect.equals("splice_region_variant")) {
                return "unknown";
            } else {
                return Data.STRING_NA;
            }
        }

        if (score < 0.4335) { //based on Liz's comment
            return "benign";
        }

        if (score < 0.9035) { //based on Liz's comment
            return "possibly";
        }

        return "probably";
    }
}
