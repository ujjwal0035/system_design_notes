public interface PriceStrategy {
    double calculatePrice(Long totalTimeMillis, Vehicle vehicle);
}