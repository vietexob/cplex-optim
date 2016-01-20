### The Wagner Diet Problem (Kimbrough & Lau, 2016, p. 44)

Given a livestock that needs to be fed with four (4) different nutrients: A, B, C, D. Our goal is to optimize the mixture of three (3) different types of grain that satisfies the four nutritional requirements. In other words, we wish to minimize the cost of grain purchases such that each nutrient is at least a minimum number.

- **Decision variables** -- **x** = [x_1, x_2, x_3]: Quantities of grains of each type to be purchased.
- **Objective function** -- Minimize the total cost of grain purcahse.
- **Constraints** -- Each nutrient (A, B, C, D) meets its minimum requirement.

**Minimize** 41x_1 + 35x_2 + 96x_3

**Subject to**:
* 2x_1 + 3x_2 + 7x_3 >= 1250
* x_1 + x2 >= 250
* 5x_1 + 3x_2 + x_3 >= 232.5
* x_1, x_2, x_3 >= 0
