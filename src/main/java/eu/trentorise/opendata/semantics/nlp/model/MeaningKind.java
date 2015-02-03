package eu.trentorise.opendata.semantics.nlp.model;

/**
 * The kind of a meaning. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 */
public enum MeaningKind {

    /**
     * An entity is something so important to us to be referred to it with a
     * name, for example 'New York City', 'Albert Einstein', 'W3C'.
     */
    ENTITY,
    /**
     * A concept is a group of objects referred with a common name, for example
     * 'city', 'person', 'organization'.
     */
    CONCEPT,
    
    UNKNOWN;
}
