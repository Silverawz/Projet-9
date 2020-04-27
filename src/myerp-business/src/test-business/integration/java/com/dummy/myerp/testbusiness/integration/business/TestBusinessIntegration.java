package com.dummy.myerp.testbusiness.integration.business;


import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.business.impl.manager.ComptabiliteManagerImpl;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.consumer.dao.impl.db.dao.ComptabiliteDaoImpl;
import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.LigneEcritureComptable;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import com.dummy.myerp.testbusiness.business.SpringRegistry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:bootstrapContext.xml")
@Sql({"classpath:sql/01_create_schema.sql", "classpath:sql/02_create_tables.sql", "classpath:sql/21_insert_data_demo.sql"})
@Transactional(propagation = Propagation.REQUIRED)
public class TestBusinessIntegration extends BusinessTestCase {

	private ComptabiliteManagerImpl comptabiliteManagerImpl;
	private ComptabiliteDaoImpl comptabiliteDaoImpl;
	private EcritureComptable ecritureComptable;
	private DaoProxy daoProxy;

	@BeforeClass
	public static void beforeClassInitialisation() {
		SpringRegistry.init();
		AbstractBusinessManager.configure(SpringRegistry.getBusinessProxy(),
				(DaoProxy) SpringRegistry.getBean("DaoProxy"), SpringRegistry.getTransactionManager());

	}

	@Test
	public void getListCompteComptable() throws FunctionalException {
		comptabiliteManagerImpl = new ComptabiliteManagerImpl();
		Assert.assertEquals(7, comptabiliteManagerImpl.getListCompteComptable().size());
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
		comptabiliteManagerImpl = new ComptabiliteManagerImpl();

		int sizeBeforeTheInsertion = comptabiliteManagerImpl.getListEcritureComptable().size();
		comptabiliteManagerImpl.insertEcritureComptable(ecritureComptable);
		Assert.assertEquals(sizeBeforeTheInsertion + 1, comptabiliteManagerImpl.getListEcritureComptable().size());

		int idLastEcritureComptableInsered = comptabiliteManagerImpl.getListEcritureComptable().size() - 1;
		ecritureComptable = comptabiliteManagerImpl.getListEcritureComptable().get(idLastEcritureComptableInsered);
		ecritureComptable.setLibelle("Un nouveau libelle");
		comptabiliteManagerImpl.updateEcritureComptable(ecritureComptable);
		Assert.assertEquals("Un nouveau libelle",
				comptabiliteManagerImpl.getListEcritureComptable().get(idLastEcritureComptableInsered).getLibelle());

		comptabiliteManagerImpl.deleteEcritureComptable(
				comptabiliteManagerImpl.getListEcritureComptable().get(idLastEcritureComptableInsered).getId());
		Assert.assertEquals(sizeBeforeTheInsertion, comptabiliteManagerImpl.getListEcritureComptable().size());
	}

	@Test
	public void addReference() throws Exception {
		comptabiliteManagerImpl = new ComptabiliteManagerImpl();
		ecritureComptable = new EcritureComptable();
		ecritureComptable.setDate(new Date());
		ecritureComptable.setJournal(new JournalComptable("AC", "Achat"));
		ecritureComptable.setLibelle("Libelle");
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(401), null, new BigDecimal(123), null));
		ecritureComptable.getListLigneEcriture()
				.add(new LigneEcritureComptable(new CompteComptable(411), null, null, new BigDecimal(123)));
		comptabiliteManagerImpl.addReference(ecritureComptable);
		comptabiliteManagerImpl.insertEcritureComptable(ecritureComptable);
		
		Assert.assertEquals("AC-2020/00001", ecritureComptable.getReference());
	}

	@Test
	public void testAddReferenceExistingReference() throws FunctionalException, NotFoundException {
		comptabiliteManagerImpl = new ComptabiliteManagerImpl();
		ecritureComptable = new EcritureComptable();
		ecritureComptable = comptabiliteManagerImpl.getListEcritureComptable().get(0);
		Assert.assertEquals("AC-2016/00001", ecritureComptable.getReference());
		comptabiliteManagerImpl.addReference(ecritureComptable);
		Assert.assertEquals("AC-2016/00002", ecritureComptable.getReference());
	}

	@Test(expected = FunctionalException.class)
	public void CheckEcritureComptable_RG_6() throws FunctionalException {
		comptabiliteManagerImpl = new ComptabiliteManagerImpl();
		ecritureComptable = new EcritureComptable();
		ecritureComptable = comptabiliteManagerImpl.getListEcritureComptable().get(0);
		ecritureComptable.getJournal()
				.setCode(comptabiliteManagerImpl.getListEcritureComptable().get(1).getJournal().getCode());
		ecritureComptable.setReference(comptabiliteManagerImpl.getListEcritureComptable().get(1).getReference());
		comptabiliteManagerImpl.checkEcritureComptable(ecritureComptable);
	}
}
