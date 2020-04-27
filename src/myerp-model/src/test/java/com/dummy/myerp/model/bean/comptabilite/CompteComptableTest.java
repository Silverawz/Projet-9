package com.dummy.myerp.model.bean.comptabilite;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompteComptableTest {

	 private static CompteComptable compteComptable;
	    private static Integer numero = 1;
	    private static String libelle = "Compte test";

	    @BeforeClass
	    public static void initBeforeClass(){
	        compteComptable = new CompteComptable(numero,libelle);
	    }

	    @Test
	    public void constructor_withDefaultValues(){
	        Assert.assertEquals(compteComptable.getLibelle(),libelle);
	        Assert.assertEquals(compteComptable.getNumero(),numero);
	    }

	    @Test
	    public void toString_NewCompteComptable(){
	        String expectedString = "CompteComptable{numero="+numero+", libelle='"+libelle+"'}";
	        String resultString = compteComptable.toString();
	        Assert.assertEquals(expectedString, resultString);
	    }

	    @Test
	    public void getByNumero_ReturnsCompteComptable_IfCompteExists(){
	        List<CompteComptable> compteComptableList = getListCompteComptable();
	        CompteComptable compteComptableFromSut = CompteComptable.getByNumero(compteComptableList, 5);
	        Assert.assertEquals(compteComptableFromSut, compteComptableList.get(5));
	    }

	    @Test
	    public void getByNumero_ReturnsNull_IfCompteDoesNotExists(){
	        List<CompteComptable> compteComptableList = getListCompteComptable();
	        CompteComptable compteComptableFromSut = CompteComptable.getByNumero(compteComptableList, 11);
	        Assert.assertEquals(null, compteComptableFromSut);
	    }

	    public List<CompteComptable> getListCompteComptable(){
	        List<CompteComptable> compteComptableList = new ArrayList<>();
	        CompteComptable compteComptable;
	        for (int i = 0; i < 10; i++) {
	            compteComptable = new CompteComptable();
	            compteComptable.setNumero(i);
	            compteComptable.setLibelle("compte numéro " + (i));
	            compteComptableList.add(compteComptable);
	        }
	        return compteComptableList;
	    }
}
