package ui11.graphics;

import ui11.SubstitutedWidget;

// ez nem teljesen tekinthető fill-nek, mert
// abban különbözik tőlük, hogy egér szempontjából átlátszatlan
public final class Empty extends SubstitutedWidget {

    private Empty() {
    }

    public static Empty empty() {
        return new Empty();
    }
}
