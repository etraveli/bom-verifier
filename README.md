[![Build Status](https://travis-ci.org/etraveli/bom-verifier.svg?branch=master)](https://travis-ci.org/etraveli/bom-verifier)

# BOM Verifier Plugin
Plugin for checking differences between previous and current configuration dependencies.

The plugin checks for differences between a local BOM file (_defaults to [project.name].bom_) and compares it to
the specified configuration (_defaults to 'runtime'_). When there is differences the check will fail and inform you of
them. Main usage is to make developers aware of changes they make to the classpath and make tracking classpath changes
easier.

## Applying the Plugin
 // TODO
 
## Usage
### Configuration
```
bomVerifier {
  bomFilePath = 'custom/path/for-reading-and-wrinte/bom'
  bomFileName = 'custom-file-name-without.file-extension'
  configuration = project.configurations.someConfiguration
  includeProjectDependencyVersions = false
}
```
<dl>
<dt>bomFilePath</dt>
    <dd>You can customize the path where the plugin writes and looks for the BOM file. <br />
        <em>Defaults to projects root dir.</em>
    </dd>
<dt>bomFileName</dt>
    <dd>You can customize the file name of the BOM file. <br /> 
        <em>Defaults to projects name.</em>
    </dd>
<dt>configuration</dt>
    <dd>You can customize the configuration to use for the BOM. <br /> 
        <em>Defaults to project.configurations.runtime.</em>
    </dd>
<dt>includeProjectDependencyVersions</dt>
    <dd>Set to 'true' if you want to include project dependency versions in the BOM. <br />
        <em>Defaults to false, i.e. * (wildcard).</em>
    </dd>
</dl>

### Tasks
`verifyBom`

`writeNewBom`
<dl>
<dt>verifyBom</dt>
    <dd>Used to verify that your runtime configurations dependencies match your dependencies declared in the BOM file. <br />
        <em> This task gets added to the Java plugins 'check' task.</em>
    </dd>
<dt>writeNewBom</dt>
<dd>Writes a new BOM file from your runtime configuration.</dd>
</dl>

# TODOs
* Add tasks to Verification group
* Add tasks descriptions
* Add inputs and outputs for incremental builds
* Add possibility to exclude dependencies