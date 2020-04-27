package com.dummy.myerp.model.bean.comptabilite;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class JournalComptableTest {

	private static JournalComptable journalComptable;
    private static String code = "AAAA";
    private static String libelle = "Test journal comptable";

    @BeforeClass
    public static void initBeforeClass(){
        journalComptable = new JournalComptable(code, libelle);
    }

    @Test
    public void constructor_withDefaultValues(){
    	Assert.assertEquals(journalComptable.getLibelle(), this.libelle);
    	Assert.assertEquals(journalComptable.getCode(), this.code);
    }

    @Test
    public void toString_NewJournalComptable(){
        String expectedString = "JournalComptable{code='"+code+"', libelle='"+libelle+"'}";
        String resultString = journalComptable.toString();
        Assert.assertEquals(expectedString, resultString);
    }

    @Test
    public void getByCode_ReturnsJournalComptable_IfCompteExists(){
        List<JournalComptable> journalComptableList = this.getJournalComptableList();
        JournalComptable journalComptableFromSut = JournalComptable.getByCode(journalComptableList, "555");
        Assert.assertEquals(journalComptableFromSut, journalComptableList.get(5));
    }

    @Test
    public void getByNumero_ReturnsNull_IfCompteDoesNotExists(){
        List<JournalComptable> journalComptableList = getJournalComptableList();
        JournalComptable journalComptableFromSut = JournalComptable.getByCode(journalComptableList, "12345");
        Assert.assertNull(journalComptableFromSut);
    }

    public List<JournalComptable> getJournalComptableList(){
        List<JournalComptable> journalComptableList = new ArrayList<>();
        JournalComptable journalComptable;
        for (int i = 0; i < 10; i++) {
            journalComptable = new JournalComptable();
            journalComptable.setCode(""+i+i+i);
            journalComptable.setLibelle("journal numéro " + journalComptable.getCode());
            journalComptableList.add(journalComptable);
        }
        return journalComptableList;
    }
}
