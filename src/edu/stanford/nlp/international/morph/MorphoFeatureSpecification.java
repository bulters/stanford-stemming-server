package edu.stanford.nlp.international.morph;

import java.io.Serializable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Morphological feature specification for surface forms in a given language.
 * Currently supported feature names are the values of MorphFeatureType.
 * 
 * @author Spence Green
 *
 */
public abstract class MorphoFeatureSpecification implements Serializable {

  private static final long serialVersionUID = -5720683653931585664L;

  public static enum MorphoFeatureType {TENSE,DEF,ASP,MOOD,NUM,GEN,CASE,PER,POSS,VOICE};
  
  protected final Set<MorphoFeatureType> activeFeatures;
  
  public MorphoFeatureSpecification() {
    activeFeatures = new HashSet<MorphoFeatureType>();
  }
  
  public void activate(MorphoFeatureType feat) {
    activeFeatures.add(feat);
  }
  
  public boolean isActive(MorphoFeatureType feat) { return activeFeatures.contains(feat); }
  
  public abstract List<String> getValues(MorphoFeatureType feat);
  
  public abstract MorphoFeatures strToFeatures(String spec);
  
  @Override
  public String toString() { return activeFeatures.toString(); }
}
