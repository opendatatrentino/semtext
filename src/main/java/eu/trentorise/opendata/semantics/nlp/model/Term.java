package eu.trentorise.opendata.semantics.nlp.model;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.trentorise.opendata.commons.NotFoundException;
import static eu.trentorise.opendata.semantics.nlp.model.Checker.checkMeaningStatus;
import static eu.trentorise.opendata.semantics.nlp.model.Checker.checkSpan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents text marked with some meaning. Text can contain spaces, like i.e.
 * "hot dog".
 *
 * @author David Leoni <david.leoni@unitn.it>
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Term implements Span, Serializable, HasMetadata {

    private int start;
    private int end;
    private ImmutableList<Meaning> meanings;

    private MeaningStatus meaningStatus;
    private Meaning selectedMeaning;
    private ImmutableMap<String, ?> metadata;

    /**
     * so serialization libraries don't complain
     */
    private Term() {
        this.start = 0;
        this.end = 0;
        this.meanings = ImmutableList.of();
        this.meaningStatus = MeaningStatus.TO_DISAMBIGUATE;
        this.selectedMeaning = null;
        this.metadata = ImmutableMap.of();
    }

    public MeaningStatus getMeaningStatus() {
        return meaningStatus;
    }

    /**
     * Constructs a Term. Meaning probabilities are stored deduplicated and
     * normalized so total sum is 1.0 . The selected meaning if not null is also
     * merged into the meanings.
     *
     * @param start 0.indexed span offset start
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * SemText container.
     * @param meaningStatus the status of the meaning.
     * @param selectedMeaning if meaning is unknown use null
     * @param meanings a new collection is created internally to store the
     * deduplicated meanings. If the selectedMeaning is present it is merged
     * in the stored meanings
     */
    private Term(int start,
            int end,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            List<Meaning> meanings,
            Map<String, ?> metadata) {
        this();

        checkSpan(start, end, "Term span is invalid!");
        checkNotNull(meanings);
        checkNotNull(meaningStatus);

        checkMeaningStatus(meaningStatus, selectedMeaning);

        normalizeMeanings(meanings, selectedMeaning);

        this.start = start;
        this.end = end;
        this.meaningStatus = meaningStatus;
        this.metadata = ImmutableMap.copyOf(metadata);
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    /**
     * Returns the sorted meanings, the first having the highest probability
     */
    public ImmutableList<Meaning> getMeanings() {
        return meanings;
    }

    @Override
    public boolean hasMetadata(String namespace) {
        return metadata.containsKey(namespace);
    }

    @Override
    public ImmutableMap<String, ?> getMetadata() {
        return metadata;
    }

    @Override
    public Object getMetadata(String namespace) {
        Object ret = metadata.get(namespace);
        if (ret == null) {
            throw new NotFoundException("There is no metadata under the namespace " + namespace + " in " + this);
        } else {
            return ret;
        }
    }

    /**
     *
     * Returns the selected meaning. Note selected meaning can be partial, that
     * is, have empty URL or meaning kind set to UNKNOWN, or both.
     *
     * <ul>
     * <li> SELECTED: Returns the selected meaning. Meaning will have a
     * non-empty ID. </li>
     * <li> TO_DISAMBIGUATE: Returns null </li>
     * <li> NOT_SURE: Returns null </li>
     * <li> VALIDATED: Returns the selected meaning, with a valid ID. </li>
     * </ul>
     */
    @Nullable
    public Meaning getSelectedMeaning() {
        return selectedMeaning;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.start;
        hash = 67 * hash + this.end;
        hash = 67 * hash + (this.meanings != null ? this.meanings.hashCode() : 0);
        hash = 67 * hash + (this.selectedMeaning != null ? this.selectedMeaning.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Term other = (Term) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (this.meanings != other.meanings && (this.meanings == null || !this.meanings.equals(other.meanings))) {
            return false;
        }
        if (this.selectedMeaning != other.selectedMeaning && (this.selectedMeaning == null || !this.selectedMeaning.equals(other.selectedMeaning))) {
            return false;
        }

        return true;
    }

    /**
     * Copy constructor
     */
    private Term(Term term) {
        this.start = term.getStart();
        this.end = term.getEnd();
        this.meanings = term.getMeanings();
        this.selectedMeaning = term.getSelectedMeaning();
        this.metadata = term.getMetadata();
    }

    /**
     * Stores in the term normalized versions of the provided meaning
     * probabilities and the selected meaning accordingly. Inputs are not
     * changed.
     *
     * @param meanings won't be changed by the method
     * @param selectedMeaning if unknown use null
     */
    private void normalizeMeanings(List<Meaning> meanings, @Nullable Meaning selectedMeaning) {
        checkNotNull(meanings);        

        Set<Meaning> dedupMeanings = new HashSet(meanings);
        if (selectedMeaning == null) {
            dedupMeanings.add(selectedMeaning);
        }

        float total = 0;
        for (Meaning m : dedupMeanings) {
            total += m.getProbability();
        }
        if (total <= 0) {
            total = dedupMeanings.size();
        }

        Meaning newSelectedMeaning = null;
        List<Meaning> mgs = new ArrayList();
        for (Meaning m : dedupMeanings) {
            Meaning newM = Meaning.of(m.getId(), m.getProbability() / total, m.getKind(), m.getName());
            if (newM.equals(selectedMeaning)) {
                newSelectedMeaning = newM;
            }
            mgs.add(newM);
        }

        Collections.sort(mgs, Collections.reverseOrder());

        this.meanings = ImmutableList.copyOf(mgs);
        this.selectedMeaning = newSelectedMeaning;
    }

    /**
     * A new term is returned with the provided meanings merged to the
     * existing ones.
     */
    public Term add(List<Meaning> meanings) {

        Set<Meaning> newMeanings = new HashSet();

        for (Meaning m1 : this.meanings) {
            newMeanings.add(m1);
        }
        for (Meaning m2 : meanings) {
            newMeanings.add(m2);
        }
                
        return this.with(ImmutableList.copyOf(newMeanings));
    }

    /**
     * Returns a new term with the provided meanings. If current selected
     * meaning is not among the new meanings, the stored meanings will
     * also have the current selected meaning.
     */
    public Term with(List<Meaning> meanings) {
        Term ret = new Term(this);
        ret.normalizeMeanings(meanings, selectedMeaning);
        return ret;
    }

    /**
     * A new term is returned with the provided meaning added to the existing
     * meanings and set as the selected meaning.
     *
     * @param meaningStatus
     * @param selectedMeaning if unknown use {@link Meaning#of()}
     */
    public Term with(MeaningStatus meaningStatus, Meaning selectedMeaning) {
        checkNotNull(selectedMeaning);
        Term ret = new Term(this);
        ret.normalizeMeanings(meanings, selectedMeaning);
        return ret;
    }

    /**
     * Factory for a Term with only one meaning. Meaning probabilities are
     * normalized so total sum is 1.0
     *
     * @param start 0-indexed offset start.
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * SemText container.
     * @param selectedMeaning if unknown use null.
     */
    public static Term of(int start,
            int end,
            MeaningStatus status,
            Meaning selectedMeaning) {

        return new Term(start,
                end,
                status,
                selectedMeaning,
                selectedMeaning == null
                        ? ImmutableList.<Meaning>of()
                        : ImmutableList.<Meaning>of(selectedMeaning),
                HasMetadata.EMPTY);
    }

    /**
     * Factory for a Term. Meaning probabilities are stored deduplicated and
     * normalized so total sum is 1.0 . The selected meaning if not null is also
     * merged in the meanings.
     *
     * @param start 0-indexed position of the span
     * @param end the position of the character immediately *after* the term
     * itself. Position is absolute with respect to the text stored in the
     * SemText container.
     * @param meaningStatus
     * @param selectedMeaning if unknown use null
     * @param meanings a new collection is created internally to store the
     * deduplicated meanings. If the selectedMeaning is present it is merged
     * in the stored meanings with top score.
     */
    public static Term of(int start,
            int end,
            MeaningStatus meaningStatus,
            @Nullable Meaning selectedMeaning,
            List<Meaning> meanings) {

        return new Term(start,
                end,
                meaningStatus,
                selectedMeaning,
                meanings,
                HasMetadata.EMPTY);
    }
                      
    
}
