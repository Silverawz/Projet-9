package com.dummy.myerp.model.bean.comptabilite;



import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;


import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class EcritureComptableTest {

    private LigneEcritureComptable createLigne(Integer pCompteComptableNumero, String pDebit, String pCredit) {
        BigDecimal vDebit = pDebit == null ? null : new BigDecimal(pDebit);
        BigDecimal vCredit = pCredit == null ? null : new BigDecimal(pCredit);
        String vLibelle = ObjectUtils.defaultIfNull(vDebit, BigDecimal.ZERO)
                                     .subtract(ObjectUtils.defaultIfNull(vCredit, BigDecimal.ZERO)).toPlainString();
        LigneEcritureComptable vRetour = new LigneEcritureComptable(new CompteComptable(pCompteComptableNumero),
                                                                    vLibelle,
                                                                    vDebit, vCredit);
        return vRetour;
    }

    @Test
    public void isEquilibree() {
        EcritureComptable vEcriture;
        vEcriture = new EcritureComptable();
       
        vEcriture.setLibelle("Equilibrée");
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "200.50", null));
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "100.50", "33"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, null, "301"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, "40", "7"));
        Assert.assertEquals(0, vEcriture.isEquilibree());
        vEcriture.getListLigneEcriture().clear();
        vEcriture.setLibelle("Non équilibrée");
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "10", null));
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "20", "1"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, null, "30"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, "1", "2"));
        Assert.assertEquals(-1, vEcriture.isEquilibree());
    }

    @Test
    public void given_ListLignes_When_GetTotalDebit_IsEqualToDebitSum(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"100.00",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"100"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"90.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"1250.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"900"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"0.75",null));
        Assert.assertEquals(ecritureComptable.getTotalDebit(), BigDecimal.valueOf(100+90.50+1250.50+0.75));
    }

    @Test
    public void given_ListLignes_When_GetTotalCredit_IsEqualToCreditSum(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"100.00",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"1500.60"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"90.50",null));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"0.25"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,null,"452"));
        ecritureComptable.getListLigneEcriture().add(createLigne(1,"0.75",null));
        Assert.assertEquals(ecritureComptable.getTotalCredit(), BigDecimal.valueOf(1500.60+0.25+452));
    }

    @Test
    public void toString_NewEcritureComptable(){
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("EcritureComptable Test");
        ecritureComptable.setJournal(new JournalComptable("BQ", "Journal Test"));
        ecritureComptable.setId(1);
        ecritureComptable.setDate(new Date());
        ecritureComptable.setReference("AA-1234-12345");
        String sep = ", ";
        String expectedString = "EcritureComptable{id="+ecritureComptable.getId()
                +sep+"journal="+ecritureComptable.getJournal()
                +sep+"reference='"+ecritureComptable.getReference()+"'"
                +sep+"date="+ecritureComptable.getDate()
                +sep+"libelle='"+ecritureComptable.getLibelle()+"'"
                +sep+"totalDebit="+ecritureComptable.getTotalDebit().toPlainString()
                +sep+"totalCredit="+ecritureComptable.getTotalCredit().toPlainString()
                +sep+"listLigneEcriture=[\n"+
                StringUtils.join(ecritureComptable.getListLigneEcriture(), "\n")+
                "\n]}";
        String resultString = ecritureComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }
}
