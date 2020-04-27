package com.dummy.myerp.model.bean.comptabilite;



import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class LigneEcritureComptableTest {

	 private static Validator validator;
	    private static CompteComptable compteComptable;

	    @BeforeClass
	    public static void setupValidatorInstance(){
	        validator = Validation.buildDefaultValidatorFactory().getValidator();
	        compteComptable = new CompteComptable();
	    }

	    @Test
	    public void whenMontantComptableMoreThanTwoDecimals_ThenShouldGiveConstraintViolation(){
	        LigneEcritureComptable ligne = new LigneEcritureComptable();
	        ligne.setCompteComptable(compteComptable);
	        ligne.setLibelle("Test ligne");
	        ligne.setCredit(new BigDecimal(100.333));
	        ligne.setDebit(new BigDecimal(1258749631245780.456));

	        Set<ConstraintViolation<LigneEcritureComptable>> violations = validator.validate(ligne);
	        Assert.assertEquals(violations.size(), 2);
	    }

	    @Test
	    public void toString_NewCompteComptable(){
	        LigneEcritureComptable ligne = new LigneEcritureComptable(
	                new CompteComptable(1,"Compte test"),
	                "Ligne test",
	                new BigDecimal(100.00),
	                new BigDecimal(0));

	        String expectedString = "LigneEcritureComptable{compteComptable="+ligne.getCompteComptable()+
	                ", libelle='"+ligne.getLibelle()+"', "+
	                "debit="+ligne.getDebit()+", "+
	                "credit="+ligne.getCredit()+"}";
	        String resultString = ligne.toString();
	        Assert.assertEquals(expectedString, resultString);
	    }
	    
}
