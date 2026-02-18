import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;

    // The window length used in this model.
    int windowLength;

    // The random number generator used by this model.
    private Random randomGenerator;

    /**
     * Constructs a language model with the given window length and a given
     * seed value. Generating texts from this model multiple times with the
     * same seed value will produce the same random texts. Good for debugging.
     */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /**
     * Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production.
     */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        In in = new In(fileName);
        String text = in.readAll();
        for (int i = 0; i <= text.length() - windowLength - 1; i++) {
            String window = text.substring(i, i + windowLength);
            char c = text.charAt(i + windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
        }
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    void calculateProbabilities(List probs) {
        int totalCount = 0;
        ListIterator iterator = probs.listIterator(0);
        while (iterator.hasNext()) {
            totalCount += iterator.next().count;
        }
        double cumulativeProbability = 0;
        iterator = probs.listIterator(0);
        while (iterator.hasNext()) {
            CharData cd = iterator.next();
            cd.p = (double) cd.count / totalCount;
            cumulativeProbability += cd.p;
            cd.cp = cumulativeProbability;
        }
    }

    // Returns a random character from the given probabilities list.
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        ListIterator iterator = probs.listIterator(0);
        while (iterator.hasNext()) {
            CharData cd = iterator.next();
            if (r < cd.cp) {
                return cd.chr;
            }
        }
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
     * Generates a random text, based on the probabilities that were learned during
     * training.
     * 
     * @param initialText     - text to start with. If initialText's last substring
     *                        of size numberOfLetters
     *                        doesn't appear as a key in Map, we generate no text
     *                        and return only the initial text.
     * @param numberOfLetters - the size of text to generate
     * @return the generated text
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }
        String generatedText = initialText;
        while (generatedText.length() - initialText.length() < textLength) {
            String window = generatedText.substring(generatedText.length() - windowLength);
            List probs = CharDataMap.get(window);
            if (probs == null) {
                return generatedText;
            }
            generatedText += getRandomChar(probs);
        }
        return generatedText;
    }

    /** Returns a string representing the map of this language model. */
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        LanguageModel model = new LanguageModel(5, 42);
        model.train("corpus.txt");
        System.out.println(model);
        String text = model.generate("The ", 100);
        System.out.println(text);
    }
}
