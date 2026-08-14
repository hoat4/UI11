package ui11;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
// TODO multithreading specifikálása
public interface ResolverProvider {

    void configure(ResolverRegistry r);
}
