package com.dummy.myerp.business.impl.manager;

import java.math.BigDecimal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.dummy.myerp.model.bean.comptabilite.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import com.dummy.myerp.business.contrat.manager.ComptabiliteManager;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Comptabilite manager implementation.
 */
@Transactional(propagation = Propagation.MANDATORY)
public class ComptabiliteManagerImpl extends AbstractBusinessManager implements ComptabiliteManager {

    // ==================== Attributs ====================


    // ==================== Constructeurs ====================
    /**
     * Instantiates a new Comptabilite manager.
     */
    public ComptabiliteManagerImpl() {
    }


    // ==================== Getters/Setters ====================
    @Override
    public List<CompteComptable> getListCompteComptable() {
        return getDaoProxy().getComptabiliteDao().getListCompteComptable();
    }


    @Override
    public List<JournalComptable> getListJournalComptable() {
        return getDaoProxy().getComptabiliteDao().getListJournalComptable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EcritureComptable> getListEcritureComptable() {
        return getDaoProxy().getComptabiliteDao().getListEcritureComptable();
    }

    @Override
    public EcritureComptable getEcritureComptableById(int id) throws NotFoundException {
        EcritureComptable ec = null;
        try {
            ec = getDaoProxy().getComptabiliteDao().getEcritureComptable(id);
        } catch (NotFoundException nfe){
            throw new NotFoundException();
        }

        return ec ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addReference(EcritureComptable pEcritureComptable) {

        String[] regex = new String[]{"/", "-"}; // Table des séparateurs identifiés
        String reference =pEcritureComptable.getReference();
        if(reference == null){
            reference = this.createReference(pEcritureComptable.getJournal().getCode(), pEcritureComptable.getDate(), regex);
        }

        String[] codeAnneeNumeroArr = extractCodeAnneeNumero(reference, regex); // [0] --> XX, [1] -> annee de l'écriture, [2] -> Numéro
        SequenceEcritureComptable sequenceEcritureComptable = null;
        if( codeAnneeNumeroArr[1] != null ){
            int annee_found = Integer.parseInt(codeAnneeNumeroArr[1]);
            try {
                sequenceEcritureComptable = getDaoProxy().getComptabiliteDao().selectSequenceEcritureComptable(annee_found, codeAnneeNumeroArr[0]);
                int numero = sequenceEcritureComptable.getDerniereValeur();
                numero++; // On incrémente le numéro existant
                codeAnneeNumeroArr[2] = calculateNumber(numero); // On recrée le #####
                getDaoProxy().getComptabiliteDao().updateSequenceEcritureComptable(Integer.parseInt(codeAnneeNumeroArr[1]), numero, codeAnneeNumeroArr[0]);
            } catch (NotFoundException nfe){
                codeAnneeNumeroArr[2] = calculateNumber(1); // On recrée le #####
                getDaoProxy().getComptabiliteDao().insertSQLinsertSequenceEcritureComptable(annee_found, Integer.parseInt(codeAnneeNumeroArr[2]),codeAnneeNumeroArr[0] );
            }

            //On reconcatène l'intégralité de la nouvelle référence. // A mettre dans une fonction pour la lisibilité.
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(codeAnneeNumeroArr[0]).append(regex[1]).append(codeAnneeNumeroArr[1]).append(regex[0]).append(codeAnneeNumeroArr[2]);
            String rebuiltReference = stringBuilder.toString();
            pEcritureComptable.setReference(rebuiltReference);// Et on rajoute la référence à l'écriture comptable.
        }

        // Bien se réferer à la JavaDoc de cette méthode !
        /* Le principe :
                1.  Remonter depuis la persitance la dernière valeur de la séquence du journal pour l'année de l'écriture
                    (table sequence_ecriture_comptable)
                2.  * S'il n'y a aucun enregistrement pour le journal pour l'année concernée :
                        1. Utiliser le numéro 1.
                    * Sinon :
                        1. Utiliser la dernière valeur + 1
                3.  Mettre à jour la référence de l'écriture avec la référence calculée (RG_Compta_5)
                4.  Enregistrer (insert/update) la valeur de la séquence en persitance
                    (table sequence_ecriture_comptable)
         */
    }

    private String createReference(String code, Date date, String[] regex) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String annee = Integer.toString(cal.get(Calendar.YEAR));
        StringBuilder sb = new StringBuilder();
        sb.append(code).append(regex[1]).append(annee).append(regex[0]).append("00000");

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public void checkEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptableUnit(pEcritureComptable);
        this.checkEcritureComptableContext(pEcritureComptable);
    }


    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion unitaires,
     * c'est à dire indépendemment du contexte (unicité de la référence, exercie comptable non cloturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */

    protected void checkEcritureComptableUnit(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== Vérification des contraintes unitaires sur les attributs de l'écriture
        Set<ConstraintViolation<EcritureComptable>> vViolations = getConstraintValidator().validate(pEcritureComptable);
        if (!vViolations.isEmpty()) {
            throw new FunctionalException("L'écriture comptable ne respecte pas les règles de gestion.",
                                          new ConstraintViolationException(
                                              "L'écriture comptable ne respecte pas les contraintes de validation",
                                              vViolations));
        }

        // ===== RG_Compta_2 : Pour qu'une écriture comptable soit valide, elle doit être équilibrée
        if (pEcritureComptable.isEquilibree() != 0) {
            throw new FunctionalException("L'écriture comptable n'est pas équilibrée.");
        }

        // ===== RG_Compta_3 : une écriture comptable doit avoir au moins 2 lignes d'écriture (1 au débit, 1 au crédit)
        int vNbrCredit = 0;
        int vNbrDebit = 0;
        for (LigneEcritureComptable vLigneEcritureComptable : pEcritureComptable.getListLigneEcriture()) {
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getCredit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrCredit++;
            }
            if (BigDecimal.ZERO.compareTo(ObjectUtils.defaultIfNull(vLigneEcritureComptable.getDebit(),
                                                                    BigDecimal.ZERO)) != 0) {
                vNbrDebit++;
            }
        }
        // On test le nombre de lignes car si l'écriture à une seule ligne
        //      avec un montant au débit et un montant au crédit ce n'est pas valable
        if (pEcritureComptable.getListLigneEcriture().size() < 2
            || vNbrCredit < 1
            || vNbrDebit < 1) {
            throw new FunctionalException(
                "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }

        // TODO ===== RG_Compta_5 : Format et contenu de la référence
        // vérifier que l'année dans la référence correspond bien à la date de l'écriture, idem pour le code journal...

        if(pEcritureComptable.getReference() != null ){
            String[] regex = new String[]{"/", "-"};
            String[] referencesExplosed = {null, null, null};
            referencesExplosed = this.extractCodeAnneeNumero(pEcritureComptable.getReference(),regex  );
            if(!referencesExplosed[0].equals(pEcritureComptable.getJournal().getCode())){
                throw new FunctionalException(
                        "Le code dans la référence ne correspond pas au code du journal de l'écriture comptable.");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(pEcritureComptable.getDate());
            String annee = Integer.toString(cal.get(Calendar.YEAR));
            if(!referencesExplosed[1].equals(annee)){
                throw new FunctionalException(
                        "L'année de la référence ne correspond pas avec l'année de l'écriture comptable.");
            }
        }
    }


    /**
     * Vérifie que l'Ecriture comptable respecte les règles de gestion liées au contexte
     * (unicité de la référence, année comptable non cloturé...)
     *
     * @param pEcritureComptable -
     * @throws FunctionalException Si l'Ecriture comptable ne respecte pas les règles de gestion
     */
    protected void checkEcritureComptableContext(EcritureComptable pEcritureComptable) throws FunctionalException {
        // ===== RG_Compta_6 : La référence d'une écriture comptable doit être unique
        if (StringUtils.isNoneEmpty(pEcritureComptable.getReference())) {
            try {
                // Recherche d'une écriture ayant la même référence
                EcritureComptable vECRef = getDaoProxy().getComptabiliteDao().getEcritureComptableByRef(
                    pEcritureComptable.getReference());

                // Si l'écriture à vérifier est une nouvelle écriture (id == null),
                // ou si elle ne correspond pas à l'écriture trouvée (id != idECRef),
                // c'est qu'il y a déjà une autre écriture avec la même référence
                if (pEcritureComptable.getId() == null
                    || !pEcritureComptable.getId().equals(vECRef.getId())) {
                    throw new FunctionalException("Une autre écriture comptable existe déjà avec la même référence.");
                }
            } catch (NotFoundException vEx) {
                // Dans ce cas, c'est bon, ça veut dire qu'on n'a aucune autre écriture avec la même référence.
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException {
        this.checkEcritureComptable(pEcritureComptable);
        getDaoProxy().getComptabiliteDao().insertEcritureComptable(pEcritureComptable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEcritureComptable(EcritureComptable pEcritureComptable) throws FunctionalException, NotFoundException {
        this.checkEcritureComptable(pEcritureComptable);
        getDaoProxy().getComptabiliteDao().updateEcritureComptable(pEcritureComptable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEcritureComptable(Integer pId) {
        getDaoProxy().getComptabiliteDao().deleteEcritureComptable(pId);
    }

    private String[] extractCodeAnneeNumero(String libelle,String[] regex){ // Rappel : FORMAT XX-AAAA/#####

        String[] annee_numero = new String[]{null, null, null}; // Table des résultats attendus
        String[] first_delim =libelle.split(regex[0]); // On sépare XX-AAA et #####
        if(first_delim.length == 2){
            annee_numero[2] = first_delim[1];  // On récupère #####
            String[] second_delim = first_delim[0].split(regex[1]); // On sépare XX et AAAA
            if(second_delim.length == 2){
                annee_numero[1] = second_delim[1]; // On récupère AAAA
                annee_numero[0] = second_delim[0]; // On récupére XX
            }
        }
        return annee_numero;
    }

    private String calculateNumber(int numero){
        String numero_converted = Integer.toString(numero);
        StringBuilder sb = new StringBuilder();
        for(int i =   5 - numero_converted.length(); i > 0; i--){ // On met dans 0 dans les cases vides
            sb.append("0");
        }
        sb.append(numero_converted);
        numero_converted = sb.toString();
        return numero_converted;
    }
}
