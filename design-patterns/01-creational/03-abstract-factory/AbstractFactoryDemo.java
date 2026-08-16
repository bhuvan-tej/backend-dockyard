/**
 * ABSTRACT FACTORY PATTERN
 * ------------------------
 * Create FAMILIES of related objects without naming their concrete classes.
 * e.g. a "Light" UI kit makes LightButton + LightCheckbox; a "Dark" kit makes
 * DarkButton + DarkCheckbox. You pick a kit once; everything stays consistent.
 *
 * The pain (without it):
 *   You `new LightButton()` here and accidentally `new DarkCheckbox()` there —
 *   the UI ends up mixing themes because nothing enforces the family.
 *
 * The fix:
 *   One factory per family produces every member. Choose the factory once and
 *   all products it makes are guaranteed to match.
 *
 * TIP TO REMEMBER:
 *   Factory Method makes ONE product; Abstract Factory makes a matching SET.
 *   "A factory OF factories" — pick the kit, get the whole consistent family.
 */

public class AbstractFactoryDemo {

    // ---- the product interfaces (the family members) ----
    interface Button   { String render(); }
    interface Checkbox { String render(); }

    // ---- the abstract factory: makes a whole family ----
    interface UIFactory {
        Button   createButton();
        Checkbox createCheckbox();
    }

    // ---- Light family ----
    static class LightButton   implements Button   { public String render() { return "[ Light Button ]"; } }
    static class LightCheckbox implements Checkbox { public String render() { return "[x] Light Checkbox"; } }
    static class LightFactory  implements UIFactory {
        public Button   createButton()   { return new LightButton(); }
        public Checkbox createCheckbox() { return new LightCheckbox(); }
    }

    // ---- Dark family ----
    static class DarkButton   implements Button   { public String render() { return "[ Dark Button ]"; } }
    static class DarkCheckbox implements Checkbox { public String render() { return "[x] Dark Checkbox"; } }
    static class DarkFactory  implements UIFactory {
        public Button   createButton()   { return new DarkButton(); }
        public Checkbox createCheckbox() { return new DarkCheckbox(); }
    }

    // Client code depends ONLY on UIFactory — it can't accidentally mix themes.
    static void renderScreen(UIFactory ui) {
        System.out.println("  " + ui.createButton().render());
        System.out.println("  " + ui.createCheckbox().render());
    }

    public static void main(String[] args) {
        System.out.println("Light theme:");
        renderScreen(new LightFactory());
        System.out.println("Dark theme:");
        renderScreen(new DarkFactory());
        System.out.println("\nExpected -> each theme renders a MATCHING button + checkbox (never mixed)");
    }
}