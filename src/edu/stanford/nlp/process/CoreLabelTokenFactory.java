package edu.stanford.nlp.process;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * Constructs {@link CoreLabel}s from Strings optionally with
 * beginning and ending (character after the end) offset positions in
 * an original text.  The makeToken method will put the token in the
 * CurrentAnnotation AND TextAnnotation keys (2 places!),
 * and optionally records
 * begin and position after offsets in BeginPositionAnnotation and
 * EndPositionAnnotation.  If the tokens are built in PTBTokenizer with
 * an "invertible" tokenizer, you will also get a BeforeAnnotation and for
 * the last token an AfterAnnotation.You can also get an empty CoreLabel token
 *
 * @author Anna Rafferty
 * @author Sonal Gupta (now implements CoreTokenFactory, you can make tokens using many options)
 */
public class CoreLabelTokenFactory implements CoreTokenFactory<CoreLabel>, LexedTokenFactory<CoreLabel> {

  final boolean addIndices;

  /**
   * Constructor for a new token factory which will add in the word, the
   * "current" annotation, and the begin/end position annotations.
   */
  public CoreLabelTokenFactory() {
    this(true);
  }

  /**
   * Constructor that allows one to choose if index annotation
   * indicating begin/end position will be included in the label.
   *
   * @param addIndices if true, begin and end position annotations will be included (this is the default)
   */
  public CoreLabelTokenFactory(boolean addIndices) {
    super();
    this.addIndices = addIndices;
  }

  /**
   * Constructs a CoreLabel as a String with a corresponding BEGIN and END position.
   * (Does not take substring).
   */
  public CoreLabel makeToken(String str, int begin, int length) {
    CoreLabel cl;
    if (addIndices) {
      cl = new CoreLabel(8); // Save a reallocation, as there will be at least 5 keys
    } else {
      cl = new CoreLabel();
    }
    cl.setWord(str);
    cl.set(TextAnnotation.class, str);
    cl.setCurrent(str);
    if(addIndices) {
      cl.set(CharacterOffsetBeginAnnotation.class, begin);
      cl.set(CharacterOffsetEndAnnotation.class, begin+length);
    }
    return cl;
  }

  public CoreLabel makeToken() {
    CoreLabel l = new CoreLabel();
    return l;
  }

  public CoreLabel makeToken(String[] keys, String[] values) {
    CoreLabel l = new CoreLabel(keys, values);
    return l;
  }

  public CoreLabel makeToken(CoreLabel labelToBeCopied) {
    CoreLabel l = new CoreLabel(labelToBeCopied);
    return l;
  }

}
