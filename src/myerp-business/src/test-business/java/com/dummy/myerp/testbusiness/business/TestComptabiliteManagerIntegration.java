package com.dummy.myerp.testbusiness.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.dummy.myerp.business.contrat.manager.ComptabiliteManager;
import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.LigneEcritureComptable;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;

public class TestComptabiliteManagerIntegration extends BusinessTestCase {

	private EcritureComptable ecritureComptable;
	private ComptabiliteManager comptabiliteManager;
	
    @Test
    public void testGetListCompteComptable() {
        comptabiliteManager = getBusinessProxy().getComptabiliteManager();
        List<CompteComptable> resultList = comptabiliteManager.getListCompteComptable();
        int nbCompteComptable = resultList.size();
        Assert.assertEquals(7, nbCompteComptable);
    }

	@Test
	public void testInsertUpdateThenDeleteForASingleEcritureComptable() throws FunctionalException, NotFoundException {
		ecritureComptable = new EcritureComptable();
		ecritureComptable.setJournal(new JournalComptable("AC", "Achat"));
		ecritureComptable.setDate(new Date());
		ecritureComptable.setLibelle("Libelle");
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(606), null, new BigDecimal(123), null));
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(706), null, null, new BigDecimal(123)));
		comptabiliteManager = getBusinessProxy().getComptabiliteManager();

		int sizeBeforeTheInsertion = comptabiliteManager.getListEcritureComptable().size();
		comptabiliteManager.insertEcritureComptable(ecritureComptable);
		Assert.assertEquals(sizeBeforeTheInsertion + 1, comptabiliteManager.getListEcritureComptable().size());

		int idLastEcritureComptableInsered = comptabiliteManager.getListEcritureComptable().size() - 1;
		ecritureComptable = comptabiliteManager.getListEcritureComptable().get(idLastEcritureComptableInsered);
		ecritureComptable.setLibelle("Un nouveau libelle");
		comptabiliteManager.updateEcritureComptable(ecritureComptable);
		Assert.assertEquals("Un nouveau libelle",
				comptabiliteManager.getListEcritureComptable().get(idLastEcritureComptableInsered).getLibelle());

		comptabiliteManager.deleteEcritureComptable(
				comptabiliteManager.getListEcritureComptable().get(idLastEcritureComptableInsered).getId());
		Assert.assertEquals(sizeBeforeTheInsertion, comptabiliteManager.getListEcritureComptable().size());
	}
	
	@Test
	public void addReference() throws Exception {
		comptabiliteManager = getBusinessProxy().getComptabiliteManager();
		ecritureComptable = new EcritureComptable();
		ecritureComptable.setDate(new Date());
		ecritureComptable.setJournal(new JournalComptable("AC", "Achat"));
		ecritureComptable.setLibelle("Libelle");
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(401), null, new BigDecimal(123), null));
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(411), null, null, new BigDecimal(123)));
		comptabiliteManager.addReference(ecritureComptable);
		comptabiliteManager.insertEcritureComptable(ecritureComptable);
		
		Assert.assertEquals("AC-2020/00001", ecritureComptable.getReference());
	}

	@Test
	public void testAddReferenceExistingReference() throws FunctionalException, NotFoundException {
		comptabiliteManager = getBusinessProxy().getComptabiliteManager();
		ecritureComptable = new EcritureComptable();
		ecritureComptable = comptabiliteManager.getListEcritureComptable().get(0);
		Assert.assertEquals("AC-2016/00001", ecritureComptable.getReference());
		comptabiliteManager.addReference(ecritureComptable);
		Assert.assertEquals("AC-2016/00002", ecritureComptable.getReference());
	}

	@Test(expected = FunctionalException.class)
	public void CheckEcritureComptable_RG_6() throws FunctionalException {
		comptabiliteManager = getBusinessProxy().getComptabiliteManager();
		ecritureComptable = new EcritureComptable();
		ecritureComptable = comptabiliteManager.getListEcritureComptable().get(0);
		ecritureComptable.getJournal()
				.setCode(comptabiliteManager.getListEcritureComptable().get(1).getJournal().getCode());
		ecritureComptable.setReference(comptabiliteManager.getListEcritureComptable().get(1).getReference());
		comptabiliteManager.checkEcritureComptable(ecritureComptable);
	}
}
