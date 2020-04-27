package com.dummy.myerp.testbusiness.integration.business;


import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.LigneEcritureComptable;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
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

    /**
     * Constructeur.
     */
    public TestBusinessIntegration() {
        super();
    }


    /*
        On test que le addRefence fonctionne correctement lorsqu'une référence n'existe pas.
     */
    @Test
    public void testAddReference() throws NotFoundException, FunctionalException {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        SpringRegistry.getBusinessProxy().getComptabiliteManager().addReference(vEcritureComptable);
        Assert.assertEquals("AC-2020/00001", vEcritureComptable.getReference());

        SpringRegistry.getBusinessProxy().getComptabiliteManager().insertEcritureComptable(vEcritureComptable);

        EcritureComptable vEcritureComptableExisting = null;
        vEcritureComptableExisting = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(3);
        Assert.assertEquals("BQ-2016/00001", vEcritureComptableExisting.getReference());

        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptableExisting);
    }

    /*
        On test qu'il y a bien deux lignes comptables.
     */
    @Test
    public void testAddReference_DeuxLignesComptables() throws FunctionalException {
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                new BigDecimal(123)));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                new BigDecimal(123)));
        SpringRegistry.getBusinessProxy().getComptabiliteManager().addReference(vEcritureComptable);
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptable);
    }

    /*
        On test que le addReference fonctionne bien lorsqu'une référence existe déjà."
     */

    @Test
    public void testAddReferenceExistingReference() throws FunctionalException, NotFoundException{
        EcritureComptable vEcritureComptableExisting = null;
        vEcritureComptableExisting = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(5);
        Assert.assertEquals("BQ-2016/00002", vEcritureComptableExisting.getReference());
        SpringRegistry.getBusinessProxy().getComptabiliteManager().addReference(vEcritureComptableExisting);
        Assert.assertEquals("BQ-2016/00003", vEcritureComptableExisting.getReference());
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptableExisting);
    }

    /*
        On test que le doublon de référence est bien détecté.
     */

    @Test(expected = FunctionalException.class)
    public void testCheckWithExistingReference() throws FunctionalException, NotFoundException{
        EcritureComptable vEcritureComptableExisting = null;
        vEcritureComptableExisting = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(3);
        Assert.assertNotEquals("BQ-2016/00005", vEcritureComptableExisting.getReference());
        vEcritureComptableExisting.setReference("BQ-2016/00002");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptableExisting);
    }

    /*
    On test la RG5 avec le code.
     */

    @Test(expected = FunctionalException.class)
    public void testCheckEcritureComptable_RG_5_code() throws FunctionalException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));

        vEcritureComptable.setReference("BC-2019/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptable);
    }

    /*
        On test la RG5 avec la date.
     */
    @Test(expected = FunctionalException.class)
    public void testCheckEcritureComptable_RG_5_date() throws FunctionalException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));

        vEcritureComptable.setReference("AC-2018/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptable);
    }

    /*
    On vérifie l'unicité de la référence.
     */

    @Test(expected = FunctionalException.class)
    public void testCheckEcritureComptable_RG_6() throws FunctionalException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));

        vEcritureComptable.setReference("AC-2016/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().checkEcritureComptable(vEcritureComptable);
    }

     /*
        On vérifie que la date de l'écriture corresponde bien avec la référence lors de l'insertion.
     */

    @Test(expected = FunctionalException.class)
    public void testInsertEcritureComptable_DateError() throws FunctionalException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("AC-2016/00002");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().insertEcritureComptable(vEcritureComptable);
    }

    /*
        On vérifie que la date de l'écriture corresponde bien avec la référence lors de l'update.
     */
    @Test(expected = FunctionalException.class)
    public void testUpdateEcritureComptable_DateError() throws FunctionalException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("AC-2016/00001");
        try {
            SpringRegistry.getBusinessProxy().getComptabiliteManager().updateEcritureComptable(vEcritureComptable);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
        On vérifie que la date de l'écriture corresponde bien avec la référence lors de l'update.
     */
    @Test(expected = FunctionalException.class)
    public void testUpdateEcritureComptable_Date() throws FunctionalException, NotFoundException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("AC-2016/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().updateEcritureComptable(vEcritureComptable);
    }

    /*
       On vérifie que la date de l'écriture corresponde bien avec la référence lors de l'update.
    */
    //@Test(expected = NotFoundException.class)
    @Test(expected = FunctionalException.class)
    public void testUpdateEcritureComptable_DateNotFound() throws FunctionalException, NotFoundException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("AC-2019/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().updateEcritureComptable(vEcritureComptable);
    }
    /*
           On vérifie que la date de l'écriture corresponde bien avec la référence lors de l'update.
        */
    @Test
    public void testUpdateEcritureComptable() throws FunctionalException, NotFoundException{
        EcritureComptable vEcritureComptable;
        vEcritureComptable = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(2);
        SpringRegistry.getBusinessProxy().getComptabiliteManager().addReference(vEcritureComptable);
        SpringRegistry.getBusinessProxy().getComptabiliteManager().updateEcritureComptable(vEcritureComptable);
    }



    /*
        On vérifie que si on fait un update sur une EcritureComptable et que la référence existe déjà ailleurs alors Throw.
     */
    @Test(expected = FunctionalException.class)
    public void testUpdateEcritureComptable_RefUniqueError() throws FunctionalException, NotFoundException{
        long twoYearsMilliseconds = 2 * 365 * 24 * 60 * 60 * 1000L;
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date(new Date().getTime() - twoYearsMilliseconds));
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("VE-2016/00002");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().updateEcritureComptable(vEcritureComptable);

    }

    /*
        On vérifie bien que si nous essayons d'insérer une Ecriture  Comptable avec une référence existantes alors Throw.
     */
    @Test(expected = FunctionalException.class)
    public void testInsertEcritureComptable_RefUniqueError() throws FunctionalException{
        long twoYearsMilliseconds = 2 * 365 * 24 * 60 * 60 * 1000L;
        EcritureComptable vEcritureComptable;
        vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date(new Date().getTime() - twoYearsMilliseconds));
        vEcritureComptable.setLibelle("Libelle");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, new BigDecimal(123),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(401),
                null, null,
                new BigDecimal(123)));
        vEcritureComptable.setReference("AC-2016/00001");
        SpringRegistry.getBusinessProxy().getComptabiliteManager().insertEcritureComptable(vEcritureComptable);
    }

    /*
    On vérifie bien qu'une fois l'élèment surppimé, nous ne pouvons plus y accéder.
     */

    @Test(expected = NotFoundException.class)
    public void testDeleteEcritureComptable() throws NotFoundException{
        EcritureComptable vEcritureComptableExisting = null;
        vEcritureComptableExisting = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(3);

        getBusinessProxy().getComptabiliteManager().deleteEcritureComptable(3);
        vEcritureComptableExisting = SpringRegistry.getBusinessProxy().getComptabiliteManager().getEcritureComptableById(3);
    }

    /*
    On vérifie que l'on récupére bien tous les comptes comptables existants.
     */

    @Test
    public void testGetListCompteComptable(){
        List<CompteComptable> compteComptableList = getBusinessProxy().getComptabiliteManager().getListCompteComptable();
        Assert.assertEquals(7, compteComptableList.size());
    }

    /*
    On vérifie que l'on récupère bien tous les journalCOmptables existants.
     */
    @Test
    public void testGetListJournalComptable(){
        List<JournalComptable> journalComptableList = getBusinessProxy().getComptabiliteManager().getListJournalComptable();
        Assert.assertEquals(4, journalComptableList.size());
    }

    /*
    On vérifie que l'on retrouve bien toutes les écritures comptables existantes.
     */

    @Test
    public void testGetListEcritureComptable(){
        List<EcritureComptable> ecritureComptableList = getBusinessProxy().getComptabiliteManager().getListEcritureComptable();
        Assert.assertEquals(5, ecritureComptableList.size());
    }


    /*
    On vérifie que tout ce passe bien quand on chercher une écriture comptable existante, que l'on récupère bien toutes ses lignes d'écritures.
     */
    @Test
    public void testGetEcritureComptableById_Work() throws NotFoundException{
        EcritureComptable ecritureComptable = getBusinessProxy().getComptabiliteManager().getEcritureComptableById(3);
        Assert.assertEquals(2, ecritureComptable.getListLigneEcriture().size());
    }

    /*
     On test bien que l'exception NotFOund est levée quand un objet n'est pas trouvé.
     */
    @Test(expected = NotFoundException.class)
    public void testGetEcritureComptableById_Throw() throws NotFoundException{
        EcritureComptable ecritureComptable = getBusinessProxy().getComptabiliteManager().getEcritureComptableById(7);
    }
}
