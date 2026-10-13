# Inspection of `TeamTrackerGTNH-0.2.1-test.jar`

## Identity

- File: `TeamTrackerGTNH-0.2.1-test.jar`
- SHA-256: `5f01982faed442bcd724e01d30ef80efb15dfd72cb46e7edb8414e0c77136721`
- `mcmod.info` version: 0.2.1
- Java classfile: major 52 (Java 8 bytecode)
- Manifest: minimal `Manifest-Version: 1.0`

## Contents

Jar contains only `com/teamtrackergtnh/*` classes and its own resources. It does **not** embed fake/stub classes under:

- `net/minecraft/*`
- `cpw/mods/*`
- `org/lwjgl/*`
- `serverutils/*`
- `com/gtnewhorizons/navigator/*`

This is good, but it does not prove ABI correctness.

## Reobfuscation-style checks already done

`javap -verbose` shows production/SRG-style references for vanilla Minecraft members, e.g.:

- `Minecraft.func_71410_x`
- `Minecraft.field_71451_h` (`renderViewEntity`)
- `RenderManager.field_78735_i` / `field_78732_j`
- `Tessellator.func_78382_b` / `func_78381_a`

The 0.2.1 POV binary has real GL constants, e.g.:

- GL_LIGHTING = 2896
- GL_DEPTH_TEST = 2929
- GL_CULL_FACE = 2884
- GL_BLEND = 3042
- GL_TEXTURE_2D = 3553
- GL_SRC_ALPHA = 770
- GL_ONE_MINUS_SRC_ALPHA = 771
- `GL_ALL_ATTRIB_BITS` compiled as 1048575

This specifically fixes a defect that existed in the old 0.2.0 hand-built jar.

## External API refs seen in binary

ServerUtilities:

- `ServerUtilitiesAPI.getTeamID(UUID): short`
- `PlayerHeadIcon(UUID)`
- `PlayerHeadIcon.bindTexture()`

Navigator:

- `NavigatorApi.registerLayerManager(LayerManager)`
- `LayerManager` ctor / recache methods
- `UniversalLayerRenderer.withRenderStep(...)`
- `UniversalLayerRenderer.withRenderPriority(int)`
- `UniversalRenderStep`
- `DrawUtils.drawRect`, `drawLabel`, `addRectToBufferWithUV`

Those signatures exist in the inspected dependency sources.

## CRITICAL binary problem discovered after build

The jar class `AlwaysOnTeamButton` contains concrete methods:

- `activate()`
- `deactivate()`
- `toggle()`

but real `Navigator 1.1.9` declares those methods `final` in `ButtonManager`.

The current jar was compiled against permissive manual stubs, so it bypassed the Java compiler check that a real dependency build would catch. This makes the current test jar unsafe to launch and is the clearest reason the project must be rebuilt against actual dependencies.

**Conclusion:** current jar is a useful forensic/reference artifact only. Do not call it production-ready.
