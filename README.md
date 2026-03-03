<p align="center">
  <img src="assets/logo.png" alt="InteractiveMC Icon" width="128" height="128">
</p>

<div align="center">
  <h1>InteractiveMC</h1>
  <p><i>Bringing physical interactions in Vivecraft.</i></p>
</div>

<div align="center" style="display: flex; justify-content: center; flex-wrap: wrap; gap: 16px; margin-bottom: 12px;">
  <a href="https://fabricmc.net/"><img src="assets/fabric_badge.png" alt="Available on Fabric"></a>
  <a href="https://neoforged.net/"><img src="assets/neoforge_badge.png" alt="Available on NeoForge"></a>
  <br>
  <a href="https://modrinth.com/mod/architectury-api"><img src="assets/architectury_api_badge.png" alt="Requires Architectury API"></a>
  <a href="https://modrinth.com/mod/vivecraft"><img src="assets/vivecraft_badge.png" alt="Requires Vivecraft"></a>
  <a href="https://modrinth.com/mod/velthoric"><img src="assets/velthoric_badge.png" alt="Requires Velthoric"></a>
</div>

## What is InteractiveMC?
**InteractiveMC** is an addon for the [Vivecraft](https://modrinth.com/mod/vivecraft) mod that introduces physical interactions powered by [Velthoric Physics](https://modrinth.com/mod/velthoric).

It enhances VR gameplay by adding real collision and interaction systems, making the Minecraft world feel more immersive.

> [!NOTE]  
> Since Velthoric is under active development, you will likely need to use [nightly builds](https://github.com/xI-Mx-Ix/Velthoric/actions/workflows/nightly.yml). Public releases occur roughly once per month and can become outdated, so we use the latest commits instead.

> [!WARNING]
> If you are playing in singleplayer, it is recommended to set `interpolation_delay_nanos` to `0` in the Velthoric client config to prevent input lag.

## Current features
- Physical interactions via Velthoric
- Player head and body collisions
- Body grabbing

## Planned features
The main goals for future development include:
- More supported versions
- Make every item physical
- Advanced APIs and features for addon developers
- Advanced interactions for weapons and other specific items
- Interaction support for popular mods such as [Create](https://modrinth.com/mod/create)
- Rendering from the physical head position (prevents clipping through walls)
- Full physical player body with bending
- Elimination of input lag

You can track all planned features and progress [here](https://github.com/Timtaran/InteractiveMC/issues?q=is%3Aissue%20state%3Aopen%20label%3Aenhancement)

## Acknowledgments
This project wouldn't be possible without these projects:
- [Vivecraft](https://modrinth.com/mod/vivecraft)
- [Velthoric](https://modrinth.com/mod/velthoric)